'use client';

import React, { memo } from 'react';
import { WeatherData } from '@/types/weather';
import DayHeader from './DayHeader';
import WeatherTimeSlot from './WeatherTimeSlot';
import ForecastHeader from './ForecastHeader';
import { useGroupedForecast } from '@/hooks/useGroupedForecast';

interface UnifiedWeatherCardProps {
  weatherData: WeatherData[];
  city: string;
}

const UnifiedWeatherCard = memo<UnifiedWeatherCardProps>(({ weatherData, city }) => {
  const groupedData = useGroupedForecast(weatherData);

  if (!weatherData.length) {
    return null;
  }

  return (
    <div className="w-full max-w-6xl mx-auto">
      <ForecastHeader city={city} count={weatherData.length} />

      <div className="space-y-6">
        {groupedData.map(({ date, items }) => (
          <div key={date} className="bg-gray-800 rounded-lg p-6">
            <DayHeader date={date} />
            
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {items.map((item, index) => (
                <WeatherTimeSlot
                  key={`${item.dt_txt}-${index}`}
                  weatherData={item}
                  index={index}
                />
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
});

UnifiedWeatherCard.displayName = 'UnifiedWeatherCard';

export default UnifiedWeatherCard;
