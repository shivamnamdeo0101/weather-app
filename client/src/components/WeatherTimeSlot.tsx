'use client';

import React, { memo, useMemo } from 'react';
import { WeatherData } from '@/types/weather';
import WeatherIcon from './WeatherIcon';
import WeatherDetails from './WeatherDetails';
import PredictionBadge from './PredictionBadge';
import { AlertTriangle, Sun } from 'lucide-react';

interface WeatherTimeSlotProps {
  weatherData: WeatherData;
  index: number;
}

const WeatherTimeSlot = memo<WeatherTimeSlotProps>(({ weatherData, index }) => {
  const formatDate = useMemo(() => {
    return (dateString: string) => {
      const date = new Date(dateString);
      return {
        date: date.toLocaleDateString('en-US', { 
          weekday: 'short', 
          month: 'short', 
          day: 'numeric' 
        }),
        time: date.toLocaleTimeString('en-US', { 
          hour: '2-digit', 
          minute: '2-digit',
          hour12: true 
        })
      };
    };
  }, []);

  const { date: formattedDate, time } = formatDate(weatherData.dt_txt);
  const hasPredictions = weatherData.predictions && weatherData.predictions.length > 0;

  return (
    <div className="bg-gray-700 rounded-lg p-4 hover:bg-gray-600 transition-colors duration-200">
      {/* Header with time and weather */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <WeatherIcon iconCode={weatherData.weather[0].icon} size="md" />
          <div>
            <p className="text-sm text-gray-300">{formattedDate}</p>
            <p className="text-xs text-gray-400">{time}</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-white">
            {Math.round(weatherData.main.temp)}°C
          </p>
          <p className="text-sm text-gray-300">
            Feels like {Math.round(weatherData.main.feels_like)}°C
          </p>
        </div>
      </div>

      {/* Weather details */}
      <WeatherDetails
        temp={weatherData.main.temp}
        feelsLike={weatherData.main.feels_like}
        tempMin={weatherData.main.temp_min}
        tempMax={weatherData.main.temp_max}
        humidity={weatherData.main.humidity}
        windSpeed={weatherData.wind.speed}
        description={weatherData.weather[0].description}
        className="mb-4"
      />

      {/* Predictions section */}
      {hasPredictions && (
        <div className="mt-4 pt-4 border-t border-gray-600">
          <div className="flex items-center gap-2 mb-3">
            <AlertTriangle className="h-4 w-4 text-orange-400" />
            <span className="text-sm font-medium text-orange-300">Predictions</span>
          </div>
          <div className="space-y-2">
            {weatherData.predictions.map((prediction, predIndex) => (
              <PredictionBadge
                key={`${weatherData.dt_txt}-pred-${predIndex}`}
                prediction={prediction}
              />
            ))}
          </div>
        </div>
      )}

      {/* No predictions indicator */}
      {!hasPredictions && (
        <div className="mt-4 pt-4 border-t border-gray-600">
          <div className="flex items-center gap-2">
            <Sun className="h-4 w-4 text-gray-400" />
            <span className="text-xs text-gray-400">No special predictions</span>
          </div>
        </div>
      )}
    </div>
  );
});

WeatherTimeSlot.displayName = 'WeatherTimeSlot';

export default WeatherTimeSlot;
