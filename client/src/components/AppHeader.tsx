'use client';

import React from 'react';
import { Cloud } from 'lucide-react';

interface AppHeaderProps {
  readonly title?: string;
  readonly subtitle?: string;
}

export default function AppHeader({ title = 'Weather Forecast', subtitle = 'Get detailed weather forecasts and predictions for any city' }: AppHeaderProps) {
  return (
    <header className="text-center mb-12 px-4 sm:px-0" role="banner">
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 mb-4">
        <Cloud className="h-10 w-10 sm:h-8 sm:w-8 text-blue-400 flex-shrink-0" />
        <h1 className="text-2xl sm:text-4xl font-bold gradient-text leading-tight break-words">
          {title}
        </h1>
      </div>
      <p className="text-gray-400 text-sm sm:text-lg max-w-xl mx-auto px-2">
        {subtitle}
      </p>
    </header>
  );
}


