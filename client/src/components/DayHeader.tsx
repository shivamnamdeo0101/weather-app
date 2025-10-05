'use client';

import React, { memo } from 'react';
import { Calendar } from 'lucide-react';

interface DayHeaderProps {
  date: string;
  className?: string;
}

const DayHeader = memo<DayHeaderProps>(({ date, className = '' }) => {
  const formattedDate = new Date(date).toLocaleDateString('en-US', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  });

  return (
    <div className={`flex items-center gap-2 mb-6 ${className}`}>
      <Calendar className="h-6 w-6 text-blue-400" />
      <h3 className="text-xl font-semibold text-white">
        {formattedDate}
      </h3>
    </div>
  );
});

DayHeader.displayName = 'DayHeader';

export default DayHeader;
