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

    return response.json();
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
          message: `Network error: ${error.message}`,
        });
      }
      throw error;
    }
  }
}
