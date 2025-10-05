'use client';

import React, { memo } from 'react';
import { AlertTriangle, Umbrella, Sun, Cloud, CloudRain } from 'lucide-react';

interface PredictionBadgeProps {
  prediction: string;
  className?: string;
}

const PredictionBadge = memo<PredictionBadgeProps>(({ prediction, className = '' }) => {
  const getPredictionIcon = (prediction: string) => {
    const lowerPrediction = prediction.toLowerCase();
    if (lowerPrediction.includes('umbrella') || lowerPrediction.includes('rain')) {
      return <Umbrella className="h-4 w-4 text-blue-400" />;
    }
    if (lowerPrediction.includes('sun') || lowerPrediction.includes('clear')) {
      return <Sun className="h-4 w-4 text-yellow-400" />;
    }
    if (lowerPrediction.includes('cloud')) {
      return <Cloud className="h-4 w-4 text-gray-400" />;
    }
    if (lowerPrediction.includes('storm') || lowerPrediction.includes('thunder')) {
      return <CloudRain className="h-4 w-4 text-purple-400" />;
    }
    return <AlertTriangle className="h-4 w-4 text-orange-400" />;
  };

  const getPredictionColor = (prediction: string) => {
    const lowerPrediction = prediction.toLowerCase();
    if (lowerPrediction.includes('umbrella') || lowerPrediction.includes('rain')) {
      return 'border-blue-500/50 bg-blue-500/10';
    }
    if (lowerPrediction.includes('sun') || lowerPrediction.includes('clear')) {
      return 'border-yellow-500/50 bg-yellow-500/10';
    }
    if (lowerPrediction.includes('cloud')) {
      return 'border-gray-500/50 bg-gray-500/10';
    }
    if (lowerPrediction.includes('storm') || lowerPrediction.includes('thunder')) {
      return 'border-purple-500/50 bg-purple-500/10';
    }
    return 'border-orange-500/50 bg-orange-500/10';
  };

  return (
    <div 
      className={`flex items-center gap-2 p-2 rounded-md border ${getPredictionColor(prediction)} ${className}`}
    >
      {getPredictionIcon(prediction)}
      <span className="text-xs text-white font-medium">{prediction}</span>
    </div>
  );
});

PredictionBadge.displayName = 'PredictionBadge';

export default PredictionBadge;
