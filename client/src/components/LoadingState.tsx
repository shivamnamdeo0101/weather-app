'use client';

import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingStateProps {
  readonly searchingCity?: string;
}

export default function LoadingState({ searchingCity }: LoadingStateProps) {
  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-gray-800 rounded-lg p-8 text-center">
        <Loader2 className="h-12 w-12 text-blue-400 animate-spin mx-auto mb-4" />
        <h3 className="text-lg font-semibold text-white mb-2">
          Loading Weather Data
        </h3>
        <p className="text-gray-400">
          Fetching forecast for {searchingCity || 'your city'}...
        </p>
      </div>
    </div>
  );
}


