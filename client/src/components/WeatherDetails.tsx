'use client';

import React, { memo } from 'react';
import { Thermometer, Droplets, Wind } from 'lucide-react';

interface WeatherDetailsProps {
  temp: number;
  feelsLike: number;
  tempMin: number;
  tempMax: number;
  humidity: number;
  windSpeed: number;
  description: string;
  className?: string;
}

const WeatherDetails = memo<WeatherDetailsProps>(({ 
  temp, 
  feelsLike, 
  tempMin, 
  tempMax, 
  humidity, 
  windSpeed, 
  description,
  className = '' 
}) => {
  return (
    <div className={`space-y-2 ${className}`}>
      <div className="flex items-center justify-between text-sm">
        <div className="flex items-center gap-1">
          <Thermometer className="h-4 w-4 text-orange-400" />
          <span className="text-gray-300">Range:</span>
        </div>
        <span className="text-white">
          {Math.round(tempMin)}° - {Math.round(tempMax)}°
        </span>
      </div>

      <div className="flex items-center justify-between text-sm">
        <div className="flex items-center gap-1">
          <Droplets className="h-4 w-4 text-blue-400" />
          <span className="text-gray-300">Humidity:</span>
        </div>
        <span className="text-white">{humidity}%</span>
      </div>

      <div className="flex items-center justify-between text-sm">
        <div className="flex items-center gap-1">
          <Wind className="h-4 w-4 text-gray-400" />
          <span className="text-gray-300">Wind:</span>
        </div>
        <span className="text-white">
          {windSpeed} m/s
        </span>
      </div>

      <div className="pt-2 border-t border-gray-600">
        <p className="text-sm text-gray-300 capitalize">
          {description}
        </p>
      </div>
    </div>
  );
});

WeatherDetails.displayName = 'WeatherDetails';

export default WeatherDetails;
