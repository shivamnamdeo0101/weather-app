'use client';

import React, { memo, useMemo } from 'react';
import { WeatherData } from '@/types/weather';
import TemperatureDayHeader from './TemperatureDayHeader';
import TemperatureItemCard from './TemperatureItemCard';
import { useFormattedForecastDate } from '@/hooks/useFormattedForecastDate';
import { useWeatherEmoji } from '@/hooks/useWeatherEmoji';

interface TemperatureListProps {
  weatherData: WeatherData[];
  city: string;
}

const TemperatureList = memo<TemperatureListProps>(({ weatherData, city }) => {
  const formatDate = useFormattedForecastDate();
  const getWeatherIcon = useWeatherEmoji();

  const groupedData = useMemo(() => {
    const groups: { [key: string]: WeatherData[] } = {};
    
    weatherData.forEach(item => {
      const date = new Date(item.dt_txt).toDateString();
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(item);
    });

    // Preserve backend order
    return Object.entries(groups).map(([date, items]) => ({
      date,
      items,
    }));
  }, [weatherData]);

  if (!weatherData.length) {
    return null;
  }

  return (
    <div className="w-full max-w-4xl mx-auto">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-white mb-2">
          Weather Forecast for {city}
        </h2>
        <p className="text-gray-400">
          {weatherData.length} forecast entries available
        </p>
      </div>

      <div className="space-y-6">
        {groupedData.map(({ date, items }) => (
          <div key={date} className="bg-gray-800 rounded-lg p-6">
            <TemperatureDayHeader date={date} />
            
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {items.map((item, index) => {
                const { date: formattedDate, time } = formatDate(item.dt_txt);
                const weatherIcon = getWeatherIcon(item.weather[0].icon);
                
                return (
                  <TemperatureItemCard
                    key={`${item.dt_txt}-${index}`}
                    emoji={weatherIcon}
                    formattedDate={formattedDate}
                    time={time}
                    temp={item.main.temp}
                    feelsLike={item.main.feels_like}
                    tempMin={item.main.temp_min}
                    tempMax={item.main.temp_max}
                    humidity={item.main.humidity}
                    windSpeed={item.wind.speed}
                    description={item.weather[0].description}
                  />
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
});

TemperatureList.displayName = 'TemperatureList';

export default TemperatureList;
