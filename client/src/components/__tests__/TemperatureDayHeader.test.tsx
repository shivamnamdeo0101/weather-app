import React from 'react';
import { render, screen } from '@testing-library/react';
import TemperatureDayHeader from '../TemperatureDayHeader';

describe('TemperatureDayHeader', () => {
  const testDate = '2025-10-19';
  const formattedDate = new Date(testDate).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  it('renders the calendar icon', () => {
    render(<TemperatureDayHeader date={testDate} />);
    const icon = screen.getByTestId('calendar-icon');
    expect(icon).toBeInTheDocument();
  });

  it('renders the correct formatted date', () => {
    render(<TemperatureDayHeader date={testDate} />);
    const dateText = screen.getByTestId('date-text');
    expect(dateText).toHaveTextContent(formattedDate);
  });
});
