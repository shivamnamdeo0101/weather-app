'use client';

import { useMemo } from 'react';

export function useWeatherEmoji() {
  return useMemo(() => {
    return (iconCode: string): string => {
      const iconMap: Record<string, string> = {
        '01d': '☀️', '01n': '🌙',
        '02d': '⛅', '02n': '☁️',
        '03d': '☁️', '03n': '☁️',
        '04d': '☁️', '04n': '☁️',
        '09d': '🌧️', '09n': '🌧️',
        '10d': '🌦️', '10n': '🌧️',
        '11d': '⛈️', '11n': '⛈️',
        '13d': '❄️', '13n': '❄️',
        '50d': '🌫️', '50n': '🌫️'
      };
      return iconMap[iconCode] || '🌤️';
    };
  }, []);
}

export default useWeatherEmoji;


