'use client';

import React from 'react';
import { Cloud } from 'lucide-react';
import OfflineToggle from './OfflineToggle';

interface AppHeaderProps {
  readonly title?: string;
  readonly subtitle?: string;
  readonly isOfflineMode?: boolean;
  readonly onOfflineModeToggle?: (enabled: boolean) => void;
  readonly cachedCitiesCount?: number;
}

export default function AppHeader({ 
  title = 'Weather Forecast', 
  subtitle = 'Get detailed weather forecasts and predictions for any city',
  isOfflineMode = false,
  onOfflineModeToggle,
  cachedCitiesCount = 0
}: AppHeaderProps) {
  return (
    <header className="text-center mb-12 px-4 sm:px-0" role="banner">
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 mb-4">
        <Cloud className="h-10 w-10 sm:h-8 sm:w-8 text-blue-400 flex-shrink-0" />
        <h1 className="text-2xl sm:text-4xl font-bold gradient-text leading-tight break-words">
          {title}
        </h1>
      </div>
      <p className="text-gray-400 text-sm sm:text-lg max-w-xl mx-auto px-2 mb-4">
        {subtitle}
      </p>
      {onOfflineModeToggle && (
        <div className="flex justify-center mt-4">
          <OfflineToggle
            isOfflineMode={isOfflineMode}
            onToggle={onOfflineModeToggle}
            cachedCitiesCount={cachedCitiesCount}
          />
        </div>
      )}
    </header>
  );
}


