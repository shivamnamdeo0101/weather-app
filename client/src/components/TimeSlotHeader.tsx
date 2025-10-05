'use client';

import React from 'react';
import WeatherIcon from './WeatherIcon';

interface TimeSlotHeaderProps {
  readonly iconCode: string;
  readonly date: string;
  readonly time: string;
  readonly tempC: number;
  readonly feelsLikeC: number;
}

export default function TimeSlotHeader({ iconCode, date, time, tempC, feelsLikeC }: TimeSlotHeaderProps) {
  return (
    <div className="flex items-center justify-between mb-4">
      <div className="flex items-center gap-2">
        <WeatherIcon iconCode={iconCode} size="md" />
        <div>
          <p className="text-sm text-gray-300">{date}</p>
          <p className="text-xs text-gray-400">{time}</p>
        </div>
      </div>
      <div className="text-right">
        <p className="text-2xl font-bold text-white">
          {Math.round(tempC)}°C
        </p>
        <p className="text-sm text-gray-300">
          Feels like {Math.round(feelsLikeC)}°C
        </p>
      </div>
    </div>
  );
}


