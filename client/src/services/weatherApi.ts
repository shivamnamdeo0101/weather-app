import { WeatherApiResponse } from '@/types/weather';
import { Logger } from '@/utils/LogLevel';
import { OfflineCacheService } from './offlineCache';

const API_BASE_URL = 'http://localhost:8081/api/weather-cache';
const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 1000; // 1s base delay (exponential)

export class WeatherError extends Error {
  constructor(public readonly data: { message: string; status?: number }) {
    super(data.message);
    this.name = 'WeatherError';
  }
}

export class WeatherApiService {
  private static async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let body: { message?: string; error?: string } = {};
      try {
        body = await response.json();
      } catch (jsonError) {
        // Response is not valid JSON, use empty object
        Logger.warn('Failed to parse error response as JSON', jsonError);
      }
      
      // Extract error message from body
      const errorMessage = body.message || body.error || `HTTP error ${response.status}`;
      
      Logger.info(`HTTP error ${response.status}:`, errorMessage);
      
      throw new WeatherError({
        message: errorMessage,
        status: response.status,
      });
    }

    let data: unknown;
    try {
      data = await response.json();
    } catch (jsonError) {
      Logger.error('Failed to parse response as JSON', jsonError);
      throw new WeatherError({
        message: 'Invalid response format from server',
        status: response.status,
      });
    }

    // Normalize nested "data.data" shapes (if any)
    let inner = (data as { data?: unknown })?.data;
    while (inner && typeof inner === 'object' && 'data' in inner) {
      inner = (inner as { data?: unknown }).data;
    }
    return (Array.isArray(inner) ? { ...(data as object), data: inner } : data) as T;
  }

  private static async fetchWithRetry(url: string, options: RequestInit): Promise<Response> {
    let lastError: Error | null = null;
    
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        Logger.info(`Fetching: ${url} (attempt ${attempt}/${MAX_RETRIES})`);
        const response = await fetch(url, options);
        return response;
      } catch (error) {
        lastError = error instanceof Error ? error : new Error(String(error));
        
        if (attempt === MAX_RETRIES) {
          Logger.error(`All ${MAX_RETRIES} retry attempts failed for ${url}`, lastError);
          break;
        }
        
        const delay = RETRY_DELAY_MS * 2 ** (attempt - 1);
        Logger.warn(`Retrying in ${delay}ms... (attempt ${attempt}/${MAX_RETRIES})`, lastError);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
    
    // Throw a proper WeatherError instead of generic Error
    throw new WeatherError({
      message: `Failed to connect to weather service after ${MAX_RETRIES} attempts. Please check your internet connection.`,
    });
  }

  /**
   * Get forecast with offline mode support
   * @param city - City name to fetch forecast for
   * @param offlineMode - If true, only use cached data. If false, try API first, fallback to cache on error.
   * @returns WeatherApiResponse
   */
  static async getForecast(city: string, offlineMode: boolean = false): Promise<WeatherApiResponse> {
    // If offline mode is enabled, check cache first
    if (offlineMode) {
      const cachedData = OfflineCacheService.get(city);
      if (cachedData) {
        Logger.info(`Using cached data for ${city} (offline mode)`);
        return cachedData;
      } else {
        throw new WeatherError({ 
          message: `No cached data available for ${city}. Please enable online mode to fetch data.` 
        });
      }
    }

    // Online mode: Try API first
    const url = `${API_BASE_URL}/forecast?city=${encodeURIComponent(city)}`;
    let apiError: WeatherError | null = null;
    
    try {
      const response = await this.fetchWithRetry(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json', accept: 'application/json' },
        signal: AbortSignal.timeout(10000),
      });
      const data = await this.handleResponse<WeatherApiResponse>(response);
      
      // Cache the successful response
      if (data.success && data.data) {
        OfflineCacheService.set(city, data);
      }
      
      return data;
    } catch (err) {
      // Preserve WeatherError if it's already one
      if (err instanceof WeatherError) {
        apiError = err;
      } else if (err instanceof Error) {
        // Handle specific error types
        if (err.name === 'AbortError') {
          apiError = new WeatherError({ 
            message: 'Request timeout. The server took too long to respond.' 
          });
          Logger.warn(`Request timeout for city: ${city}`);
        } else {
          // Network or other errors
          apiError = new WeatherError({ 
            message: err.message || 'Network error. Please check your connection.' 
          });
          Logger.error(`Network error for city: ${city}`, err);
        }
      } else {
        // Unknown error type
        apiError = new WeatherError({ 
          message: 'An unexpected error occurred while fetching weather data.' 
        });
        Logger.error(`Unexpected error type for city: ${city}`, err);
      }

      // Try to use cached data as fallback (except for 404 - city not found)
      // Don't fallback to cache for 404 because cache would also not have it
      const errorStatus = apiError.data.status;
      if (errorStatus === undefined || errorStatus !== 404) {
        const cachedData = OfflineCacheService.get(city);
        if (cachedData) {
          Logger.info(`Using cached data for ${city} (API request failed: ${apiError.message})`);
          return cachedData;
        }
      }

      // No cache available or it's a 404 error, throw the error
      throw apiError;
    }
  }
}
