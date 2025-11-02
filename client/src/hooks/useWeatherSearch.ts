'use client';

import { useState, useCallback } from 'react';
import { WeatherData } from '@/types/weather';
import { WeatherApiService, WeatherError } from '@/services/weatherApi';

/**
 * Convert error to user-friendly message
 */
function getUserFriendlyErrorMessage(err: unknown, requestedCity: string): string {
  if (err instanceof WeatherError) {
    const status = err.data.status;
    const serverMsg = err.message.trim();

    // Handle specific HTTP status codes
    if (status) {
      switch (status) {
        case 400:
          return serverMsg || 'Invalid request. Please check your city name and try again.';
        
        case 401:
          return serverMsg || 'Unauthorized to access weather provider.';
        
        case 404:
          // Check if server message indicates city not found
          if (serverMsg.toLowerCase().includes('city not found') || 
              serverMsg.toLowerCase().includes('not found')) {
            return `City "${requestedCity}" not found. Please check the spelling and try again.`;
          }
          return serverMsg || `No weather forecast found for "${requestedCity}". Please try a different city.`;
        
        case 429:
          return serverMsg || 'Too many requests. Please wait a moment before trying again.';
        
        case 502:
        case 503:
        case 504:
          return serverMsg || 'Weather service is temporarily unavailable. Please try again later.';
        
        case 500:
          return serverMsg || 'Server error occurred. Please try again later.';
        
        default:
          return serverMsg || 'An unexpected server error occurred. Please try again.';
      }
    }

    // No HTTP status - likely network/timeout/cache error
    return serverMsg || 'Network error. Please check your connection and try again.';
  }

  // Non-WeatherError fallback
  if (err instanceof Error) {
    return err.message || 'An unexpected error occurred. Please try again.';
  }

  // Unknown error type
  return 'An unexpected error occurred. Please try again.';
}

export interface UseWeatherSearchState {
  weatherData: WeatherData[];
  currentCity: string;
  isLoading: boolean;
  error: string | null;
  searchingCity: string;
}

export interface UseWeatherSearchReturn extends UseWeatherSearchState {
  handleSearch: (city: string, offlineMode?: boolean) => Promise<void>;
  clearError: () => void;
}

export function useWeatherSearch(): UseWeatherSearchReturn {
  const [weatherData, setWeatherData] = useState<WeatherData[]>([]);
  const [currentCity, setCurrentCity] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchingCity, setSearchingCity] = useState<string>('');

  const handleSearch = useCallback(async (city: string, offlineMode: boolean = false) => {
    setIsLoading(true);
    setError(null);
    setSearchingCity(city);

    try {
      const response = await WeatherApiService.getForecast(city, offlineMode);
      if (response.success && response.data) {
        setWeatherData(response.data);
        setCurrentCity(city);
      } else {
        setError(response.message || 'Failed to fetch weather data');
        setWeatherData([]);
      }
    } catch (err) {
      // Normalize errors and map HTTP status codes to user-friendly messages
      const errorMessage = getUserFriendlyErrorMessage(err, city);
      setError(errorMessage);
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


