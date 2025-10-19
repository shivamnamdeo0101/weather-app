import { WeatherApiResponse } from '@/types/weather';
import { Logger } from '@/utils/LogLevel';

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
      const body = await response.json().catch(() => ({}));
      Logger.info('HTTP error', response.status, body);
      throw new WeatherError({
        message: body.message || `HTTP error ${response.status}`,
        status: response.status,
      });
    }

    const data = await response.json().catch(() => ({}));

    // Normalize nested "data.data" shapes (if any)
    let inner = data?.data;
    while (inner?.data) inner = inner.data;
    return (Array.isArray(inner) ? { ...data, data: inner } : data) as T;
  }

  private static async fetchWithRetry(url: string, options: RequestInit): Promise<Response> {
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        Logger.info(`Fetching: ${url} (attempt ${attempt})`);
        return await fetch(url, options);
      } catch (error) {
        if (attempt === MAX_RETRIES) {
          Logger.error(`All retry attempts failed for ${url}`, error);
          throw error;
        }
        const delay = RETRY_DELAY_MS * 2 ** (attempt - 1);
        Logger.warn(`Retrying in ${delay}ms...`, error);
        await new Promise(res => setTimeout(res, delay));
      }
    }
    throw new Error('Unreachable');
  }

  static async getForecast(city: string): Promise<WeatherApiResponse> {
    const url = `${API_BASE_URL}/forecast?city=${encodeURIComponent(city)}`;
    try {
      const response = await this.fetchWithRetry(url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json', accept: 'application/json' },
        signal: AbortSignal.timeout(10000),
      });
      return await this.handleResponse<WeatherApiResponse>(response);
    } catch (err) {
      if (err instanceof Error) {
        if (err.name === 'AbortError') {
          Logger.error('Request timeout for city:', city);
          throw new WeatherError({ message: 'Request timeout' });
        }
        Logger.error('Unexpected error for city:', city, err);
        throw new WeatherError({ message: err.message });
      }
      throw err;
    }
  }
}
