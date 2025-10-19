import React from 'react';
import { render, screen } from '@testing-library/react';
import DayHeader from "../DayHeader";

jest.mock('lucide-react', () => ({
  Calendar: () => <svg data-testid="calendar-icon" />,
}));

describe('DayHeader', () => {
  it('renders the formatted date correctly', () => {
    const testDate = '2025-10-19T00:00:00Z'; // ISO format
    render(<DayHeader date={testDate} />);

    const formatted = new Date(testDate).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });

    expect(screen.getByText(formatted)).toBeInTheDocument();
  });

  it('renders the calendar icon', () => {
    render(<DayHeader date="2025-10-19" />);
    expect(screen.getByTestId('calendar-icon')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const customClass = 'bg-red-500';
    const { container } = render(<DayHeader date="2025-10-19" className={customClass} />);
    expect(container.firstChild).toHaveClass(customClass);
  });
});
