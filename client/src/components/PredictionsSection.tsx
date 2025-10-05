'use client';

import React from 'react';
import PredictionBadge from './PredictionBadge';
import { AlertTriangle, Sun } from 'lucide-react';

interface PredictionsSectionProps {
  readonly predictions: string[];
  readonly dtTxt: string;
}

export default function PredictionsSection({ predictions, dtTxt }: PredictionsSectionProps) {
  const hasPredictions = predictions && predictions.length > 0;

  return (
    <div className="mt-4 pt-4 border-t border-gray-600">
      {hasPredictions ? (
        <>
          <div className="flex items-center gap-2 mb-3">
            <AlertTriangle className="h-4 w-4 text-orange-400" />
            <span className="text-sm font-medium text-orange-300">Predictions</span>
          </div>
          <div className="space-y-2">
            {predictions.map((prediction, predIndex) => (
              <PredictionBadge
                key={`${dtTxt}-pred-${predIndex}`}
                prediction={prediction}
              />
            ))}
          </div>
        </>
      ) : (
        <div className="flex items-center gap-2">
          <Sun className="h-4 w-4 text-gray-400" />
          <span className="text-xs text-gray-400">No special predictions</span>
        </div>
      )}
    </div>
  );
}


