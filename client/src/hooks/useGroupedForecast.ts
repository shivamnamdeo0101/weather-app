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

    return Object.entries(groups).map(([date, items]) => {
      const sortedItems = [...items].sort(
        (a, b) => new Date(a.dt_txt).getTime() - new Date(b.dt_txt).getTime()
      );
      return { date, items: sortedItems };
    });
  }, [weatherData]);
}

export default useGroupedForecast;


