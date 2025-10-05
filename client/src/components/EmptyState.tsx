'use client';

import React from 'react';
import { Cloud } from 'lucide-react';

export default function EmptyState() {
  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-gray-800 rounded-lg p-12 text-center">
        <Cloud className="h-16 w-16 text-gray-400 mx-auto mb-6" />
        <h3 className="text-2xl font-bold text-white mb-4">
          Welcome to Weather Forecast
        </h3>
        <p className="text-gray-400 text-lg mb-6">
          Enter a city name above to get detailed weather forecasts and predictions
        </p>
        <div className="flex flex-wrap justify-center gap-4 text-sm text-gray-500">
          <span>• Real-time weather data</span>
          <span>• 5-day forecasts</span>
          <span>• Smart predictions</span>
          <span>• Detailed analytics</span>
        </div>
      </div>
    </div>
  );
}


