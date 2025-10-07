'use client';

import { useState, useCallback } from 'react';
import { WeatherData } from '@/types/weather';
import { WeatherApiService, WeatherError } from '@/services/weatherApi';

export interface UseWeatherSearchState {
  weatherData: WeatherData[];
  currentCity: string;
  isLoading: boolean;
  error: string | null;
  searchingCity: string;
}

export interface UseWeatherSearchReturn extends UseWeatherSearchState {
  handleSearch: (city: string) => Promise<void>;
  clearError: () => void;
}

export function useWeatherSearch(): UseWeatherSearchReturn {
  const [weatherData, setWeatherData] = useState<WeatherData[]>([]);
  const [currentCity, setCurrentCity] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchingCity, setSearchingCity] = useState<string>('');

  const handleSearch = useCallback(async (city: string) => {
    setIsLoading(true);
    setError(null);
    setSearchingCity(city);

    try {
      const response = await WeatherApiService.getForecast(city);
      if (response.success && response.data) {
        setWeatherData(response.data);
        setCurrentCity(city);
      } else {
        setError(response.message || 'Failed to fetch weather data');
        setWeatherData([]);
      }
    } catch (err) {
      // Normalize errors and map HTTP status codes to user-friendly messages
      const mapWeatherError = (we: WeatherError | Error, requestedCity: string) => {
        if (we instanceof WeatherError) {
          const status = we.status;
          const serverMsg = we.message;
          if (status) {
            switch (status) {
              case 400:
                return serverMsg || 'Invalid request. Please check your input.';
              case 401:
                return serverMsg || 'Unauthorized to access weather provider.';
              case 404:
                return serverMsg || `No forecast found for ${requestedCity}.`;
              case 429:
                return serverMsg || 'Too many requests. Please wait before retrying.';
              case 502:
              case 503:
              case 504:
                return serverMsg || 'Weather service is temporarily unavailable. Please try again later.';
              case 500:
              default:
                return serverMsg || 'An unexpected server error occurred. Please try again later.';
            }
          }
          // No HTTP status on the WeatherError -- likely network/timeout
          return serverMsg || 'Network error. Please check your connection and try again.';
        }
        // Non-WeatherError fallback
  return we.message || 'An unexpected error occurred. Please try again.';
      };

      setError(mapWeatherError(err as Error, city));
      setWeatherData([]);
    } finally {
      setIsLoading(false);
      setSearchingCity('');
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
    setSearchingCity('');
  }, []);

  return {
    weatherData,
    currentCity,
    isLoading,
    error,
    searchingCity,
    handleSearch,
    clearError,
  };
}

export default useWeatherSearch;


