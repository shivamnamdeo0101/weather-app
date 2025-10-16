import { WeatherApiResponse } from '@/types/weather';

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
      console.debug('Response normalization failed', e);
    }

    return json as T;
  }

  private static async fetchWithRetry(url: string, options: RequestInit, retries = MAX_RETRIES): Promise<Response> {
    let attempt = 0;
    while (true) {
      try {
        return await fetch(url, options);
      } catch (error) {
        attempt++;
        if (attempt > retries) throw error;
        const delay = RETRY_DELAY_MS * 2 ** (attempt - 1);
        console.warn(`Fetch failed (attempt ${attempt}/${retries}). Retrying in ${delay}ms...`);
        await new Promise((res) => setTimeout(res, delay));
      }
    }
  }

  static async getForecast(city: string): Promise<WeatherApiResponse> {
    try {
      const response = await this.fetchWithRetry(
        `${API_BASE_URL}/forecast?city=${encodeURIComponent(city)}`,
        {
          method: 'GET',
          headers: {
            'accept': 'application/json',
            'Content-Type': 'application/json',
          },
          signal: AbortSignal.timeout(10000),
        }
      );

      return await this.handleResponse<WeatherApiResponse>(response);
    } catch (error) {
      if (error instanceof Error) {
        if (error.name === 'AbortError') {
          throw new WeatherError({
            message: 'Request timeout. Please try again.',
          });
        }
        throw new WeatherError({
          message: error.message,
        });
      }
      throw error;
    }
  }
}
