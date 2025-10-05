
'use client';

import React from 'react';
import SearchBar from '@/components/SearchBar';
import UnifiedWeatherCard from '@/components/UnifiedWeatherCard';
import AppHeader from '@/components/AppHeader';
import ErrorAlert from '@/components/ErrorAlert';
import LoadingState from '@/components/LoadingState';
import EmptyState from '@/components/EmptyState';
import { useWeatherSearch } from '@/hooks/useWeatherSearch';

export default function Home() {
  const {
    weatherData,
    currentCity,
    isLoading,
    error,
    searchingCity,
    handleSearch,
    clearError,
  } = useWeatherSearch();

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <AppHeader />

        {/* Search Bar */}
        <div className="mb-8">
          <SearchBar 
            onSearch={handleSearch} 
            isLoading={isLoading}
          />
        </div>

        {/* Error Display */}
        {error && (
          <ErrorAlert error={error} searchingCity={searchingCity} onClose={clearError} />
        )}

        {/* Loading State */}
        {isLoading && (
          <LoadingState searchingCity={searchingCity} />
        )}

        {/* Weather Data Display */}
        {!isLoading && weatherData.length > 0 && (
          <UnifiedWeatherCard 
            weatherData={weatherData} 
            city={currentCity} 
          />
        )}

        {/* Empty State */}
        {!isLoading && weatherData.length === 0 && !error && (
          <EmptyState />
        )}
      </div>
    </div>
  );
}
