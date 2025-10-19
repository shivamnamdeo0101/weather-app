import React from 'react';
import { render, screen } from '@testing-library/react';
import TimeSlotHeader from '../TimeSlotHeader';

// Mock WeatherIcon to simplify testing
jest.mock('../WeatherIcon', () => ({
  __esModule: true,
  default: ({ iconCode, size }: { iconCode: string; size: string }) => (
    <div data-testid="weather-icon">{iconCode}-{size}</div>
  ),
}));

describe('TimeSlotHeader', () => {
  const props = {
    iconCode: '01d',
    date: 'Sun, Oct 19, 2025',
    time: '02:00 PM',
    tempC: 25,
    feelsLikeC: 27,
  };

  it('renders the weather icon with correct props', () => {
    render(<TimeSlotHeader {...props} />);
    const icon = screen.getByTestId('weather-icon');
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveTextContent('01d-md'); // iconCode + size
  });

  it('renders the date and time', () => {
    render(<TimeSlotHeader {...props} />);
    expect(screen.getByText(props.date)).toBeInTheDocument();
    expect(screen.getByText(props.time)).toBeInTheDocument();
  });

  it('renders the temperature and feels like values', () => {
    render(<TimeSlotHeader {...props} />);
    expect(screen.getByText(`${Math.round(props.tempC)}°C`)).toBeInTheDocument();
    expect(screen.getByText(`Feels like ${Math.round(props.feelsLikeC)}°C`)).toBeInTheDocument();
  });
});
