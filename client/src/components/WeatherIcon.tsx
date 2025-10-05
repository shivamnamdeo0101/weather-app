'use client';

import React, { memo } from 'react';

interface WeatherIconProps {
  iconCode: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const WeatherIcon = memo<WeatherIconProps>(({ iconCode, size = 'md', className = '' }) => {
  const getWeatherIcon = (code: string) => {
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
    return iconMap[code] || '🌤️';
  };

  const sizeClasses = {
    sm: 'text-lg',
    md: 'text-2xl',
    lg: 'text-3xl'
  };

  return (
    <span className={`${sizeClasses[size]} ${className}`}>
      {getWeatherIcon(iconCode)}
    </span>
  );
});

WeatherIcon.displayName = 'WeatherIcon';

export default WeatherIcon;
