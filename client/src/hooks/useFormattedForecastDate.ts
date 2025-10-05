'use client';

import { useMemo } from 'react';

export interface FormattedDateTime {
  date: string;
  time: string;
}

export function useFormattedForecastDate() {
  return useMemo(() => {
    return (dateString: string): FormattedDateTime => {
      const date = new Date(dateString);
      return {
        date: date.toLocaleDateString('en-US', {
          weekday: 'short',
          month: 'short',
          day: 'numeric',
        }),
        time: date.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: true,
        }),
      };
    };
  }, []);
}

export default useFormattedForecastDate;


