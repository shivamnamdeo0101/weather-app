'use client';

import React from 'react';

interface ForecastHeaderProps {
  readonly city: string;
  readonly count: number;
}

export default function ForecastHeader({ city, count }: ForecastHeaderProps) {
  return (
    <div className="mb-6">
      <h2 className="text-2xl font-bold text-white mb-2">
        Weather Forecast for {city}
      </h2>
      <p className="text-gray-400">
        {count} forecast entries with predictions
      </p>
    </div>
  );
}


