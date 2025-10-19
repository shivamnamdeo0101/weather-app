import { render } from '@testing-library/react';
import React from 'react';
import { useFormattedForecastDate } from '../useFormattedForecastDate';

function HookWrapper({ dateString }: { dateString: string }) {
  const formatDate = useFormattedForecastDate();
  const formatted = formatDate(dateString);
  return (
    <div data-date={formatted.date} data-time={formatted.time}></div>
  );
}

describe('useFormattedForecastDate', () => {
  it('formats date and time correctly', () => {
    const testDate = '2025-10-19 14:30:00';
    const { container } = render(<HookWrapper dateString={testDate} />);
    const div = container.firstChild as HTMLElement;

    expect(div?.dataset.date).toBeDefined();
    expect(div?.dataset.time).toBeDefined();
    expect(div?.dataset.date).toMatch(/[A-Za-z]{3}, [A-Za-z]{3} \d{1,2}/);
    expect(div?.dataset.time).toMatch(/\d{2}:\d{2} [AP]M/);
  });
});
