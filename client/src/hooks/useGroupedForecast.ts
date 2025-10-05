'use client';

import { useMemo } from 'react';
import { WeatherData } from '@/types/weather';

export interface GroupedForecastItem {
  date: string;
  items: WeatherData[];
}

export function useGroupedForecast(weatherData: WeatherData[]): GroupedForecastItem[] {
  return useMemo(() => {
    const groups: { [key: string]: WeatherData[] } = {};

    weatherData.forEach((item) => {
      const date = new Date(item.dt_txt).toDateString();
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(item);
    });

    // Preserve the original order from backend for both dates and items
    return Object.entries(groups).map(([date, items]) => ({
      date,
      items,
    }));
  }, [weatherData]);
}

export default useGroupedForecast;


