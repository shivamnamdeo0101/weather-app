'use client';

import React from 'react';
import { AlertCircle } from 'lucide-react';

interface ErrorAlertProps {
  readonly error: string;
  readonly searchingCity?: string;
  readonly onClose?: () => void;
}

export default function ErrorAlert({ error, searchingCity, onClose }: ErrorAlertProps) {
  return (
    <div className="max-w-4xl mx-auto mb-8">
      <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 flex items-center gap-3">
        <AlertCircle className="h-5 w-5 text-red-400 flex-shrink-0" />
        <div className="flex-1">
          <p className="text-red-300 font-medium">Error</p>
          <p className="text-red-200 text-sm">{error}</p>
          {searchingCity && (
            <p className="text-red-200 text-xs mt-1">
              Failed to load weather for: {searchingCity}
            </p>
          )}
        </div>
        <button
          onClick={onClose}
          className="text-red-400 hover:text-red-300 transition-colors"
        >
          ×
        </button>
      </div>
    </div>
  );
}


