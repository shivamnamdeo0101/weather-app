/* eslint-disable react/display-name */
'use client';

import React, { memo, useMemo } from 'react';
import { WeatherData } from '@/types/weather';
import { AlertTriangle, Umbrella, Sun, Cloud, CloudRain } from 'lucide-react';

interface PredictionsListProps {
  weatherData: WeatherData[];
}

const PredictionsListComponent: React.FC<Readonly<PredictionsListProps>> = ({ weatherData }) => {
  // Named component (typed) — will memoize on export
  const predictionsWithTime = useMemo(() => {
    return weatherData
      .filter(item => item.predictions && item.predictions.length > 0)
      .map(item => {
        const date = new Date(item.dt_txt);
        return {
          ...item,
          formattedTime: {
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
          }
        };
      });
  }, [weatherData]);
  const getPredictionIcon = useMemo(() => {
    return (prediction: string) => {
      const lowerPrediction = prediction.toLowerCase();
      if (lowerPrediction.includes('umbrella') || lowerPrediction.includes('rain')) {
        return <Umbrella className="h-5 w-5 text-blue-400" />;
      }
      if (lowerPrediction.includes('sun') || lowerPrediction.includes('clear')) {
        return <Sun className="h-5 w-5 text-yellow-400" />;
      }
      if (lowerPrediction.includes('cloud')) {
        return <Cloud className="h-5 w-5 text-gray-400" />;
      }
      if (lowerPrediction.includes('storm') || lowerPrediction.includes('thunder')) {
        return <CloudRain className="h-5 w-5 text-purple-400" />;
      }
      return <AlertTriangle className="h-5 w-5 text-orange-400" />;
    };
  }, []);

  const getPredictionColor = useMemo(() => {
    return (prediction: string) => {
      const lowerPrediction = prediction.toLowerCase();
      if (lowerPrediction.includes('umbrella') || lowerPrediction.includes('rain')) {
        return 'border-blue-500 bg-blue-500/10';
      }
      if (lowerPrediction.includes('sun') || lowerPrediction.includes('clear')) {
        return 'border-yellow-500 bg-yellow-500/10';
      }
      if (lowerPrediction.includes('cloud')) {
        return 'border-gray-500 bg-gray-500/10';
      }
      if (lowerPrediction.includes('storm') || lowerPrediction.includes('thunder')) {
        return 'border-purple-500 bg-purple-500/10';
      }
      return 'border-orange-500 bg-orange-500/10';
    };
  }, []);

  if (!predictionsWithTime.length) {
    return (
      <div className="w-full max-w-4xl mx-auto">
        <div className="bg-gray-800 rounded-lg p-6 text-center">
          <AlertTriangle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-white mb-2">No Predictions Available</h3>
          <p className="text-gray-400">
            There are no weather predictions for the selected time period.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-4xl mx-auto">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-white mb-2">
          Weather Predictions
        </h2>
        <p className="text-gray-400">
          {predictionsWithTime.length} prediction{predictionsWithTime.length !== 1 ? 's' : ''} available
        </p>
      </div>

      <div className="space-y-4">
        {predictionsWithTime.map((item, index) => (
          <div 
            key={`${item.dt_txt}-${index}`}
            className="bg-gray-800 rounded-lg p-6 hover:bg-gray-700 transition-colors duration-200"
          >
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2">
                  <span className="text-2xl">
                    {item.weather[0].icon.includes('d') ? '☀️' : '🌙'}
                  </span>
                  <div>
                    <p className="text-lg font-semibold text-white">
                      {item.formattedTime.date}
                    </p>
                    <p className="text-sm text-gray-300">
                      {item.formattedTime.time}
                    </p>
                  </div>
                </div>
              </div>
              
              <div className="text-right">
                <p className="text-xl font-bold text-white">
                  {Math.round(item.main.temp)}°C
                </p>
                <p className="text-sm text-gray-300 capitalize">
                  {item.weather[0].description}
                </p>
              </div>
            </div>

            <div className="grid gap-3">
              {item.predictions.map((prediction, predIndex) => (
                <div 
                  key={`${item.dt_txt}-pred-${predIndex}`}
                  className={`flex items-center gap-3 p-4 rounded-lg border ${getPredictionColor(prediction)}`}
                >
                  {getPredictionIcon(prediction)}
                  <div className="flex-1">
                    <p className="text-white font-medium">{prediction}</p>
                    <p className="text-sm text-gray-300">
                      Valid for {item.formattedTime.fullDate} at {item.formattedTime.time}
                    </p>
                  </div>
                  <div className="text-sm text-gray-400">
                    {item.main.humidity}% humidity
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-4 pt-4 border-t border-gray-600">
              <div className="flex items-center justify-between text-sm text-gray-400">
                <span>Wind: {item.wind.speed} m/s</span>
                <span>Pressure: {item.main.pressure} hPa</span>
                <span>Feels like: {Math.round(item.main.feels_like)}°C</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// Set display name explicitly for debug and tooling
PredictionsListComponent.displayName = 'PredictionsList';

const PredictionsList = memo(PredictionsListComponent);
PredictionsList.displayName = 'PredictionsList';

export default PredictionsList;
