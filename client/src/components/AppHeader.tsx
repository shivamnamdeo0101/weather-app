'use client';

import React from 'react';
import { Cloud } from 'lucide-react';

interface AppHeaderProps {
  readonly title?: string;
  readonly subtitle?: string;
}

export default function AppHeader({ title = 'Weather Forecast', subtitle = 'Get detailed weather forecasts and predictions for any city' }: AppHeaderProps) {
  return (
    <div className="text-center mb-12">
      <div className="flex items-center justify-center gap-3 mb-4">
        <Cloud className="h-8 w-8 text-blue-400" />
        <h1 className="text-4xl font-bold gradient-text">
          {title}
        </h1>
      </div>
      <p className="text-gray-400 text-lg">
        {subtitle}
      </p>
    </div>
  );
}


