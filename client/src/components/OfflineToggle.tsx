'use client';

import React from 'react';
import { WifiOff, Wifi } from 'lucide-react';

interface OfflineToggleProps {
  readonly isOfflineMode: boolean;
  readonly onToggle: (enabled: boolean) => void;
  readonly cachedCitiesCount?: number;
}

export default function OfflineToggle({ 
  isOfflineMode, 
  onToggle,
  cachedCitiesCount = 0 
}: OfflineToggleProps) {
  const handleToggle = () => {
    onToggle(!isOfflineMode);
  };

  return (
    <div className="flex items-center gap-3">
      <button
        onClick={handleToggle}
        className={`
          relative inline-flex h-7 w-14 items-center rounded-full transition-colors
          focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-gray-900
          ${isOfflineMode ? 'bg-gray-600' : 'bg-blue-600'}
        `}
        role="switch"
        aria-checked={!isOfflineMode}
        aria-label={isOfflineMode ? 'Enable online mode' : 'Disable online mode'}
        title={isOfflineMode ? 'Offline mode - Click to go online' : 'Online mode - Click to go offline'}
      >
        <span
          className={`
            inline-block h-5 w-5 transform rounded-full bg-white transition-transform
            ${isOfflineMode ? 'translate-x-1' : 'translate-x-8'}
          `}
        />
      </button>
      
      <div className="flex items-center gap-2">
        {isOfflineMode ? (
          <WifiOff className="h-5 w-5 text-gray-400" aria-hidden="true" />
        ) : (
          <Wifi className="h-5 w-5 text-blue-400" aria-hidden="true" />
        )}
        <span className="text-sm text-gray-300">
          {isOfflineMode ? 'Offline' : 'Online'}
          {isOfflineMode && cachedCitiesCount > 0 && (
            <span className="ml-1 text-xs text-gray-400">
              ({cachedCitiesCount} cached)
            </span>
          )}
        </span>
      </div>
    </div>
  );
}

