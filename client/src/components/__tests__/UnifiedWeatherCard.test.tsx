// src/components/__tests__/UnifiedWeatherCard.test.tsx
import React from 'react';
import { render, screen } from '@testing-library/react';
import UnifiedWeatherCard from '../UnifiedWeatherCard';
import { WeatherData } from '@/types/weather';

// Mock child components to simplify testing
jest.mock('../ForecastHeader', () => ({
  __esModule: true,
  default: ({ city, count }: { city: string; count: number }) => (
    <div data-testid="forecast-header">{`${city} - ${count}`}</div>
  ),
}));

jest.mock('../DayHeader', () => ({
  __esModule: true,
  default: ({ date }: { date: string }) => <div data-testid="day-header">{date}</div>,
}));

jest.mock('../WeatherTimeSlot', () => ({
  __esModule: true,
  default: ({ weatherData }: { weatherData: WeatherData }) => (
    <div data-testid="time-slot">{weatherData.dt_txt}</div>
  ),
}));

// Mock hook
jest.mock('@/hooks/useGroupedForecast', () => ({
  useGroupedForecast: jest.fn((weatherData: WeatherData[]) => {
    // Return grouped data as an array
    const groups: { date: string; items: WeatherData[] }[] = [];
    const map: Record<string, WeatherData[]> = {};
    weatherData.forEach(item => {
      const date = new Date(item.dt_txt).toDateString();
      if (!map[date]) map[date] = [];
      map[date].push(item);
    });
    for (const date in map) {
      groups.push({ date, items: map[date] });
    }
    return groups;
  }),
}));

describe('UnifiedWeatherCard', () => {
  const mockWeatherData: WeatherData[] = [
    {
      dt_txt: '2025-10-19 14:00:00',
      main: {
        temp: 25,
        feels_like: 26,
        temp_min: 22,
        temp_max: 28,
        pressure: 1012,
        sea_level: 1012,
        grnd_level: 1008,
        humidity: 60,
        temp_kf: 0,
      },
      weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
      wind: { speed: 5, deg: 180, gust: 7 },
      predictions: [],
    },
    {
      dt_txt: '2025-10-19 17:00:00',
      main: {
        temp: 22,
        feels_like: 23,
        temp_min: 20,
        temp_max: 24,
        pressure: 1011,
        sea_level: 1011,
        grnd_level: 1007,
        humidity: 55,
        temp_kf: 0,
      },
      weather: [{ id: 801, main: 'Clouds', description: 'few clouds', icon: '02d' }],
      wind: { speed: 3, deg: 200, gust: 5 },
      predictions: [],
    },
    {
      dt_txt: '2025-10-20 10:00:00',
      main: {
        temp: 21,
        feels_like: 21,
        temp_min: 19,
        temp_max: 23,
        pressure: 1013,
        sea_level: 1013,
        grnd_level: 1009,
        humidity: 50,
        temp_kf: 0,
      },
      weather: [{ id: 802, main: 'Clouds', description: 'scattered clouds', icon: '03d' }],
      wind: { speed: 4, deg: 150, gust: 6 },
      predictions: [],
    },
  ];

  it('renders ForecastHeader with city and count', () => {
    render(<UnifiedWeatherCard weatherData={mockWeatherData} city="New York" />);
    const header = screen.getByTestId('forecast-header');
    expect(header).toHaveTextContent('New York - 3');
  });

  it('renders correct number of DayHeaders', () => {
    render(<UnifiedWeatherCard weatherData={mockWeatherData} city="New York" />);
    const dayHeaders = screen.getAllByTestId('day-header');
    expect(dayHeaders.length).toBe(2); // two unique dates: 19th and 20th
  });

  it('renders correct number of WeatherTimeSlots', () => {
    render(<UnifiedWeatherCard weatherData={mockWeatherData} city="New York" />);
    const timeSlots = screen.getAllByTestId('time-slot');
    expect(timeSlots.length).toBe(mockWeatherData.length);
  });

  it('renders nothing for empty weatherData', () => {
    const { container } = render(<UnifiedWeatherCard weatherData={[]} city="Empty City" />);
    expect(container.firstChild).toBeNull();
  });
});
