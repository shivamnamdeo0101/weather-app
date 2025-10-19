import React from 'react';
import { render, screen } from '@testing-library/react';
import TemperatureList from '../TemperatureList';
import { WeatherData } from '@/types/weather';

// Mock child components to simplify testing
jest.mock('../TemperatureDayHeader', () => ({
  __esModule: true,
  default: ({ date }: { date: string }) => <div data-testid="day-header">{date}</div>,
}));

jest.mock('../TemperatureItemCard', () => ({
  __esModule: true,
  default: (props: any) => <div data-testid="item-card">{props.emoji}</div>,
}));

// Mock hooks
jest.mock('@/hooks/useFormattedForecastDate', () => ({
  useFormattedForecastDate: () => (dt_txt: string) => ({
    date: new Date(dt_txt).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' }),
    time: new Date(dt_txt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
  }),
}));

jest.mock('@/hooks/useWeatherEmoji', () => ({
  useWeatherEmoji: () => (_icon: string) => '☀️',
}));

describe('TemperatureList', () => {
  const mockWeatherData: WeatherData[] = [
    {
      dt_txt: '2025-10-19 14:00:00',
      main: {
        temp: 25,
        feels_like: 26,
        temp_min: 22,
        temp_max: 28,
        pressure: 1012,
        sea_level: 1015,
        grnd_level: 1008,
        humidity: 60,
        temp_kf: 0.5,
      },
      weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
      wind: { speed: 5, deg: 180, gust: 7 },
      predictions: ['sunny'],
    },
    {
      dt_txt: '2025-10-19 17:00:00',
      main: {
        temp: 22,
        feels_like: 23,
        temp_min: 20,
        temp_max: 24,
        pressure: 1010,
        sea_level: 1013,
        grnd_level: 1007,
        humidity: 55,
        temp_kf: 0.3,
      },
      weather: [{ id: 801, main: 'Clouds', description: 'few clouds', icon: '02d' }],
      wind: { speed: 3, deg: 200, gust: 5 },
      predictions: ['cloudy'],
    },
    {
      dt_txt: '2025-10-20 10:00:00',
      main: {
        temp: 21,
        feels_like: 21,
        temp_min: 19,
        temp_max: 23,
        pressure: 1015,
        sea_level: 1018,
        grnd_level: 1010,
        humidity: 50,
        temp_kf: 0.2,
      },
      weather: [{ id: 802, main: 'Clouds', description: 'scattered clouds', icon: '03d' }],
      wind: { speed: 4, deg: 150, gust: 6 },
      predictions: ['partly cloudy'],
    },
  ];

  it('renders city header and forecast count', () => {
    render(<TemperatureList weatherData={mockWeatherData} city="New York" />);
    expect(screen.getByText('Weather Forecast for New York')).toBeInTheDocument();
    expect(screen.getByText(`${mockWeatherData.length} forecast entries available`)).toBeInTheDocument();
  });

  it('renders correct number of day headers', () => {
    render(<TemperatureList weatherData={mockWeatherData} city="New York" />);
    const dayHeaders = screen.getAllByTestId('day-header');
    expect(dayHeaders.length).toBe(2); // 2 unique dates: 19th and 20th
  });

  it('renders all temperature item cards', () => {
    render(<TemperatureList weatherData={mockWeatherData} city="New York" />);
    const itemCards = screen.getAllByTestId('item-card');
    expect(itemCards.length).toBe(mockWeatherData.length);
    itemCards.forEach(card => expect(card).toHaveTextContent('☀️')); // emoji
  });

  it('renders nothing for empty data', () => {
    const { container } = render(<TemperatureList weatherData={[]} city="Empty City" />);
    expect(container.firstChild).toBeNull();
  });
});
