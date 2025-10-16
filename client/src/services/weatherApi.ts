import { WeatherApiResponse } from '@/types/weather';
import { Logger } from '@/utils/LogLevel';

const API_BASE_URL = 'http://localhost:8081/api/weather-cache';
const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 1000; // initial delay 1s, doubles each retry

export class WeatherError extends Error {
  public status?: number;

  constructor({ message, status }: { message: string; status?: number }) {
    super(message);
    this.name = 'WeatherError';
    this.status = status;
  }
}

export class WeatherApiService {
 
  private static async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      Logger.info('HTTP error', response.status, errorData);
      throw new WeatherError({
        message: errorData.message || `HTTP error! status: ${response.status}`,
        status: response.status,
      });
    }

    const json = await response.json().catch(() => ({}));

    try {
      if (json && typeof json === 'object' && json.data && typeof json.data === 'object') {
        let inner = json.data;
        while (inner && typeof inner === 'object' && inner.data) {
          inner = inner.data;
        }
        if (Array.isArray(inner)) {
          return { ...json, data: inner } as unknown as T;
        }
      }
    } catch (e) {
      Logger.info('Response normalization failed', e);
    }

    return json as T;
  }

  private static async fetchWithRetry(
    url: string,
    options: RequestInit,
    retries = MAX_RETRIES
  ): Promise<Response> {
    let attempt = 0;
    while (true) {
      try {
        Logger.info(`Fetching URL: ${url} (attempt ${attempt + 1})`);
        const res = await fetch(url, options);
        return res;
      } catch (error) {
        attempt++;
        if (attempt > retries) {
         Logger.error(`All retry attempts failed for ${url}`);
          throw error;
        }
        const delay = RETRY_DELAY_MS * 2 ** (attempt - 1);
        Logger.warn(`Fetch failed (attempt ${attempt}/${retries}). Retrying in ${delay}ms...`, error);
        await new Promise((res) => setTimeout(res, delay));
      }
    }
  }

  static async getForecast(city: string): Promise<WeatherApiResponse> {
    try {
      const url = `${API_BASE_URL}/forecast?city=${encodeURIComponent(city)}`;
      const response = await this.fetchWithRetry(url, {
        method: 'GET',
        headers: {
          accept: 'application/json',
          'Content-Type': 'application/json',
        },
        signal: AbortSignal.timeout(10000),
      });

      return await this.handleResponse<WeatherApiResponse>(response);
    } catch (error) {
      if (error instanceof Error) {
        if (error.name === 'AbortError') {
          Logger.error('Request timeout for city:', city);
          throw new WeatherError({ message: 'Request timeout. Please try again.' });
        }
        Logger.error('Unexpected error for city:', city, error);
        throw new WeatherError({ message: error.message });
      }
      throw error;
    }
  }
}
