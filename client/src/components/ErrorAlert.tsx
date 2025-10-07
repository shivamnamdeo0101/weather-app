 'use client';

import React, { useMemo } from 'react';
import { AlertCircle } from 'lucide-react';
import UI from '@/constants/ui';

interface ErrorAlertProps {
  readonly error: string;
  readonly searchingCity?: string;
  readonly onClose?: () => void;
}

function ErrorAlert({ error, searchingCity, onClose }: ErrorAlertProps) {
  const icon = useMemo(() => {
    if (!error) return UI.Icons.ERROR;
    const lower = error.toLowerCase();
    if (lower.includes('network') || lower.includes('connection') || lower.includes('timeout')) {
      return UI.Icons.NETWORK;
    }
    return UI.Icons.ERROR;
  }, [error]);

  return (
    <div className="max-w-4xl mx-auto mb-8">
      <div
        className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 flex items-center gap-3"
        role="alert"
        aria-live="assertive"
      >
        <AlertCircle className="h-5 w-5 text-red-400 flex-shrink-0" aria-hidden />
        <div className="flex-1">
          <p className="text-red-300 font-medium">
            <span className="sr-only">{UI.Labels.ERROR_TITLE}: </span>
            {UI.Labels.ERROR_TITLE}
            <span aria-hidden className="ml-2">{icon}</span>
          </p>
          <p className="text-red-200 text-sm">
            {error}
            <span aria-hidden className="ml-2">{icon}</span>
          </p>
          {searchingCity && (
            <p className="text-red-200 text-xs mt-1">
              Failed to load weather for: {searchingCity}
            </p>
          )}
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label="Close error"
          className="text-red-400 hover:text-red-300 transition-colors"
        >
          ×
        </button>
      </div>
    </div>
  );
}

export default React.memo(ErrorAlert);


