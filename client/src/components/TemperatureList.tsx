'use client';

import React, { memo, useMemo } from 'react';
import { WeatherData } from '@/types/weather';
import { Thermometer, Calendar, Droplets, Wind } from 'lucide-react';

interface TemperatureListProps {
  weatherData: WeatherData[];
  city: string;
}

const TemperatureList = memo<TemperatureListProps>(({ weatherData, city }) => {
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

  const getWeatherIcon = useMemo(() => {
    return (iconCode: string) => {
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

  const groupedData = useMemo(() => {
    const groups: { [key: string]: WeatherData[] } = {};
    
    weatherData.forEach(item => {
      const date = new Date(item.dt_txt).toDateString();
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(item);
    });

    return Object.entries(groups).map(([date, items]) => {
      const sortedItems = [...items].sort((a, b) => new Date(a.dt_txt).getTime() - new Date(b.dt_txt).getTime());
      return {
        date,
        items: sortedItems
      };
    });
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
            <div className="flex items-center gap-2 mb-4">
              <Calendar className="h-5 w-5 text-blue-400" />
              <h3 className="text-lg font-semibold text-white">
                {new Date(date).toLocaleDateString('en-US', { 
                  weekday: 'long', 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric' 
                })}
              </h3>
            </div>
            
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {items.map((item, index) => {
                const { date: formattedDate, time } = formatDate(item.dt_txt);
                const weatherIcon = getWeatherIcon(item.weather[0].icon);
                
                return (
                  <div 
                    key={`${item.dt_txt}-${index}`}
                    className="bg-gray-700 rounded-lg p-4 hover:bg-gray-600 transition-colors duration-200"
                  >
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center gap-2">
                        <span className="text-2xl">{weatherIcon}</span>
                        <div>
                          <p className="text-sm text-gray-300">{formattedDate}</p>
                          <p className="text-xs text-gray-400">{time}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-2xl font-bold text-white">
                          {Math.round(item.main.temp)}°C
                        </p>
                        <p className="text-sm text-gray-300">
                          Feels like {Math.round(item.main.feels_like)}°C
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
                          {Math.round(item.main.temp_min)}° - {Math.round(item.main.temp_max)}°
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-sm">
                        <div className="flex items-center gap-1">
                          <Droplets className="h-4 w-4 text-blue-400" />
                          <span className="text-gray-300">Humidity:</span>
                        </div>
                        <span className="text-white">{item.main.humidity}%</span>
                      </div>

                      <div className="flex items-center justify-between text-sm">
                        <div className="flex items-center gap-1">
                          <Wind className="h-4 w-4 text-gray-400" />
                          <span className="text-gray-300">Wind:</span>
                        </div>
                        <span className="text-white">
                          {item.wind.speed} m/s
                        </span>
                      </div>

                      <div className="pt-2 border-t border-gray-600">
                        <p className="text-sm text-gray-300 capitalize">
                          {item.weather[0].description}
                        </p>
                      </div>
                    </div>
                  </div>
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
