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
      if (err instanceof WeatherError) {
        setError(err.message);
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
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


