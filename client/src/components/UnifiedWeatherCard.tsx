'use client';

import React, { memo, useMemo } from 'react';
import { WeatherData } from '@/types/weather';
import { Thermometer, Calendar, Droplets, Wind, AlertTriangle, Umbrella, Sun, Cloud, CloudRain } from 'lucide-react';

interface UnifiedWeatherCardProps {
  weatherData: WeatherData[];
  city: string;
}

const UnifiedWeatherCard = memo<UnifiedWeatherCardProps>(({ weatherData, city }) => {
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
        }),
        fullDate: date.toLocaleDateString('en-US', { 
          weekday: 'long', 
          year: 'numeric', 
          month: 'long', 
          day: 'numeric' 
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

  const getPredictionIcon = useMemo(() => {
    return (prediction: string) => {
      const lowerPrediction = prediction.toLowerCase();
      if (lowerPrediction.includes('umbrella') || lowerPrediction.includes('rain')) {
        return <Umbrella className="h-4 w-4 text-blue-400" />;
      }
      if (lowerPrediction.includes('sun') || lowerPrediction.includes('clear')) {
        return <Sun className="h-4 w-4 text-yellow-400" />;
      }
      if (lowerPrediction.includes('cloud')) {
        return <Cloud className="h-4 w-4 text-gray-400" />;
      }
      if (lowerPrediction.includes('storm') || lowerPrediction.includes('thunder')) {
        return <CloudRain className="h-4 w-4 text-purple-400" />;
      }
      return <AlertTriangle className="h-4 w-4 text-orange-400" />;
    };
  }, []);

  const getPredictionColor = useMemo(() => {
    return (prediction: string) => {
      const lowerPrediction = prediction.toLowerCase();
      if (lowerPrediction.includes('umbrella') || lowerPrediction.includes('rain')) {
        return 'border-blue-500/50 bg-blue-500/10';
      }
      if (lowerPrediction.includes('sun') || lowerPrediction.includes('clear')) {
        return 'border-yellow-500/50 bg-yellow-500/10';
      }
      if (lowerPrediction.includes('cloud')) {
        return 'border-gray-500/50 bg-gray-500/10';
      }
      if (lowerPrediction.includes('storm') || lowerPrediction.includes('thunder')) {
        return 'border-purple-500/50 bg-purple-500/10';
      }
      return 'border-orange-500/50 bg-orange-500/10';
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
    <div className="w-full max-w-6xl mx-auto">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-white mb-2">
          Weather Forecast for {city}
        </h2>
        <p className="text-gray-400">
          {weatherData.length} forecast entries with predictions
        </p>
      </div>

      <div className="space-y-6">
        {groupedData.map(({ date, items }) => (
          <div key={date} className="bg-gray-800 rounded-lg p-6">
            <div className="flex items-center gap-2 mb-6">
              <Calendar className="h-6 w-6 text-blue-400" />
              <h3 className="text-xl font-semibold text-white">
                {new Date(date).toLocaleDateString('en-US', { 
                  weekday: 'long', 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric' 
                })}
              </h3>
            </div>
            
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {items.map((item, index) => {
                const { date: formattedDate, time } = formatDate(item.dt_txt);
                const weatherIcon = getWeatherIcon(item.weather[0].icon);
                const hasPredictions = item.predictions && item.predictions.length > 0;
                
                return (
                  <div 
                    key={`${item.dt_txt}-${index}`}
                    className="bg-gray-700 rounded-lg p-4 hover:bg-gray-600 transition-colors duration-200"
                  >
                    {/* Header with time and weather */}
                    <div className="flex items-center justify-between mb-4">
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

                    {/* Weather details */}
                    <div className="space-y-2 mb-4">
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

                    {/* Predictions section */}
                    {hasPredictions && (
                      <div className="mt-4 pt-4 border-t border-gray-600">
                        <div className="flex items-center gap-2 mb-3">
                          <AlertTriangle className="h-4 w-4 text-orange-400" />
                          <span className="text-sm font-medium text-orange-300">Predictions</span>
                        </div>
                        <div className="space-y-2">
                          {item.predictions.map((prediction, predIndex) => (
                            <div 
                              key={`${item.dt_txt}-pred-${predIndex}`}
                              className={`flex items-center gap-2 p-2 rounded-md border ${getPredictionColor(prediction)}`}
                            >
                              {getPredictionIcon(prediction)}
                              <span className="text-xs text-white font-medium">{prediction}</span>
                            </div>
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
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
});

UnifiedWeatherCard.displayName = 'UnifiedWeatherCard';

export default UnifiedWeatherCard;
