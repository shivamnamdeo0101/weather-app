'use client';

import React, { useState, useCallback, memo } from 'react';
import { Search, Loader2 } from 'lucide-react';

interface SearchBarProps {
  onSearch: (city: string) => Promise<void>;
  isLoading: boolean;
  disabled?: boolean;
}

const SearchBar = memo<SearchBarProps>(({ onSearch, isLoading, disabled = false }) => {
  const [city, setCity] = useState('');

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    if (!city.trim() || isLoading || disabled) return;
    
    const trimmedCity = city.trim();
    setCity('');
    await onSearch(trimmedCity);
  }, [city, onSearch, isLoading, disabled]);

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setCity(e.target.value);
  }, []);

  return (
    <form onSubmit={handleSubmit} className="w-full max-w-md mx-auto">
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          {isLoading ? (
            <Loader2 className="h-5 w-5 text-gray-400 animate-spin" />
          ) : (
            <Search className="h-5 w-5 text-gray-400" />
          )}
        </div>
        <input
          type="text"
          value={city}
          onChange={handleInputChange}
          placeholder="Enter city name..."
          disabled={isLoading || disabled}
          className="w-full pl-10 pr-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
        />
        <button
          type="submit"
          disabled={!city.trim() || isLoading || disabled}
          className="absolute right-2 top-1/2 transform -translate-y-1/2 px-4 py-1.5 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white text-sm font-medium rounded-md transition-colors duration-200"
        >
          {isLoading ? 'Searching...' : 'Search'}
        </button>
      </div>
    </form>
  );
});

SearchBar.displayName = 'SearchBar';

export default SearchBar;
