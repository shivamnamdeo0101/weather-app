'use client';

import React from 'react';
import { Calendar } from 'lucide-react';

interface TemperatureDayHeaderProps {
  readonly date: string;
}

export default function TemperatureDayHeader({ date }: TemperatureDayHeaderProps) {
  return (
    <div className="flex items-center gap-2 mb-4">
      <Calendar className="h-5 w-5 text-blue-400" />
      <h3 className="text-lg font-semibold text-white">
        {new Date(date).toLocaleDateString('en-US', {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        })}
      </h3>
    </div>
  );
}


