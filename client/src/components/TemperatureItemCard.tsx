'use client';

import React from 'react';
import { Thermometer, Droplets, Wind } from 'lucide-react';

interface TemperatureItemCardProps {
  readonly emoji: string;
  readonly formattedDate: string;
  readonly time: string;
  readonly temp: number;
  readonly feelsLike: number;
  readonly tempMin: number;
  readonly tempMax: number;
  readonly humidity: number;
  readonly windSpeed: number;
  readonly description: string;
}

export default function TemperatureItemCard({
  emoji,
  formattedDate,
  time,
  temp,
  feelsLike,
  tempMin,
  tempMax,
  humidity,
  windSpeed,
  description,
}: TemperatureItemCardProps) {
  return (
    <div className="bg-gray-700 rounded-lg p-4 hover:bg-gray-600 transition-colors duration-200">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <span className="text-2xl">{emoji}</span>
          <div>
            <p className="text-sm text-gray-300">{formattedDate}</p>
            <p className="text-xs text-gray-400">{time}</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-white">
            {Math.round(temp)}°C
          </p>
          <p className="text-sm text-gray-300">
            Feels like {Math.round(feelsLike)}°C
          </p>
        </div>
      </div>

      <div className="space-y-2">
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
          <span className="text-white">{windSpeed} m/s</span>
        </div>

        <div className="pt-2 border-t border-gray-600">
          <p className="text-sm text-gray-300 capitalize">
            {description}
          </p>
        </div>
      </div>
    </div>
  );
}


