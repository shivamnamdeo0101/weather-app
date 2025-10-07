import { WeatherApiResponse } from '@/types/weather';

const API_BASE_URL = 'http://localhost:8081/api/weather-cache';

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

    // Defensive normalization: some server responses may wrap the actual array
    // as `data.data` (e.g., upstream CustomResponse saved into the cache). If we
    // see that pattern, unwrap it so FE always receives `data` as WeatherData[].
    try {
      if (json && typeof json === 'object' && json.data && typeof json.data === 'object') {
        let inner = json.data;
        // Unwrap nested `.data` layers until we hit an array or a non-object
        while (inner && typeof inner === 'object' && inner.data) {
          inner = inner.data;
        }
        if (Array.isArray(inner)) {
          // return a normalized payload where `data` is the array
          return { ...json, data: inner } as unknown as T;
        }
      }
    } catch (e) {
      console.debug('Response normalization failed', e);
    }

    return json as T;
  }

  static async getForecast(city: string): Promise<WeatherApiResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/forecast?city=${encodeURIComponent(city)}`, {
        method: 'GET',
        headers: {
          'accept': 'application/json',
          'Content-Type': 'application/json',
        },
        // Add timeout to prevent hanging requests
        signal: AbortSignal.timeout(10000), // 10 seconds timeout
      });

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
