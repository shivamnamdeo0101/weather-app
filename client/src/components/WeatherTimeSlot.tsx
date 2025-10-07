'use client';

import React, { memo } from 'react';
import { WeatherData } from '@/types/weather';
import WeatherDetails from './WeatherDetails';
import TimeSlotHeader from './TimeSlotHeader';
import PredictionsSection from './PredictionsSection';
import { useFormattedForecastDate } from '@/hooks/useFormattedForecastDate';

interface WeatherTimeSlotProps {
  weatherData: WeatherData;
  index: number;
}

const WeatherTimeSlot = memo<WeatherTimeSlotProps>(({ weatherData, index: _index }) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const _unusedIndex = _index;
  const formatDate = useFormattedForecastDate();
  const { date: formattedDate, time } = formatDate(weatherData.dt_txt);

  return (
    <div className="bg-gray-700 rounded-lg p-4 hover:bg-gray-600 transition-colors duration-200">
      {/* Header with time and weather */}
      <TimeSlotHeader
        iconCode={weatherData.weather[0].icon}
        date={formattedDate}
        time={time}
        tempC={weatherData.main.temp}
        feelsLikeC={weatherData.main.feels_like}
      />

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
      <PredictionsSection
        predictions={weatherData.predictions}
        dtTxt={weatherData.dt_txt}
      />
    </div>
  );
});

WeatherTimeSlot.displayName = 'WeatherTimeSlot';

export default WeatherTimeSlot;
