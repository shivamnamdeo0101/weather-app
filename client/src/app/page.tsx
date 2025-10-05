'use client';

import React, { useState, useCallback, useMemo } from 'react';
import { WeatherData } from '@/types/weather';
import { WeatherApiService, WeatherError } from '@/services/weatherApi';
import SearchBar from '@/components/SearchBar';
import TemperatureList from '@/components/TemperatureList';
import PredictionsList from '@/components/PredictionsList';
import { Cloud, AlertCircle, Loader2 } from 'lucide-react';

export default function Home() {
  const [weatherData, setWeatherData] = useState<WeatherData[]>([]);
  const [currentCity, setCurrentCity] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = useCallback(async (city: string) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await WeatherApiService.getForecast(city);
      
      if (response.success && response.data) {
        setWeatherData(response.data);
        setCurrentCity(city);
      } else {
        setError(response.message || 'Failed to fetch weather data');
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
    }
  }, []);

  const hasPredictions = useMemo(() => {
    return weatherData.some(item => item.predictions && item.predictions.length > 0);
  }, [weatherData]);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-12">
          <div className="flex items-center justify-center gap-3 mb-4">
            <Cloud className="h-8 w-8 text-blue-400" />
            <h1 className="text-4xl font-bold gradient-text">
              Weather Forecast
            </h1>
          </div>
          <p className="text-gray-400 text-lg">
            Get detailed weather forecasts and predictions for any city
          </p>
        </div>

        {/* Search Bar */}
        <div className="mb-8">
          <SearchBar 
            onSearch={handleSearch} 
            isLoading={isLoading}
          />
        </div>

        {/* Error Display */}
        {error && (
          <div className="max-w-4xl mx-auto mb-8">
            <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 flex items-center gap-3">
              <AlertCircle className="h-5 w-5 text-red-400 flex-shrink-0" />
              <div className="flex-1">
                <p className="text-red-300 font-medium">Error</p>
                <p className="text-red-200 text-sm">{error}</p>
              </div>
              <button
                onClick={clearError}
                className="text-red-400 hover:text-red-300 transition-colors"
              >
                ×
              </button>
            </div>
          </div>
        )}

        {/* Loading State */}
        {isLoading && (
          <div className="max-w-4xl mx-auto">
            <div className="bg-gray-800 rounded-lg p-8 text-center">
              <Loader2 className="h-12 w-12 text-blue-400 animate-spin mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-white mb-2">
                Loading Weather Data
              </h3>
              <p className="text-gray-400">
                Fetching forecast for {currentCity || 'your city'}...
              </p>
            </div>
          </div>
        )}

        {/* Weather Data Display */}
        {!isLoading && weatherData.length > 0 && (
          <div className="space-y-8">
            {/* Temperature List */}
            <TemperatureList 
              weatherData={weatherData} 
              city={currentCity} 
            />

            {/* Predictions List */}
            {hasPredictions && (
              <PredictionsList weatherData={weatherData} />
            )}
          </div>
        )}

        {/* Empty State */}
        {!isLoading && weatherData.length === 0 && !error && (
          <div className="max-w-4xl mx-auto">
            <div className="bg-gray-800 rounded-lg p-12 text-center">
              <Cloud className="h-16 w-16 text-gray-400 mx-auto mb-6" />
              <h3 className="text-2xl font-bold text-white mb-4">
                Welcome to Weather Forecast
              </h3>
              <p className="text-gray-400 text-lg mb-6">
                Enter a city name above to get detailed weather forecasts and predictions
              </p>
              <div className="flex flex-wrap justify-center gap-4 text-sm text-gray-500">
                <span>• Real-time weather data</span>
                <span>• 5-day forecasts</span>
                <span>• Smart predictions</span>
                <span>• Detailed analytics</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
