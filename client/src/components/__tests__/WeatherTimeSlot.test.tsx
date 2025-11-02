// src/components/__tests__/WeatherTimeSlot.test.tsx
import React from 'react';
import { render, screen } from '@testing-library/react';
import WeatherTimeSlot from '../WeatherTimeSlot';
import { WeatherData } from '@/types/weather';

// Mock child components
jest.mock('../TimeSlotHeader', () => ({
  __esModule: true,
  default: ({ date, time, iconCode, tempC, feelsLikeC }: { date: string; time: string; iconCode: string; tempC: number; feelsLikeC: number }) => (
    <div data-testid="time-slot-header">
      {date}-{time}-{iconCode}-{tempC}-{feelsLikeC}
    </div>
  ),
}));

jest.mock('../WeatherDetails', () => ({
  __esModule: true,
  default: ({ description }: { description: string }) => <div data-testid="weather-details">{description}</div>,
}));

jest.mock('../PredictionsSection', () => ({
  __esModule: true,
  default: ({ predictions }: { predictions: string[] }) => (
    <div data-testid="predictions-section">{predictions.join(',')}</div>
  ),
}));

// Mock hook
jest.mock('@/hooks/useFormattedForecastDate', () => ({
  useFormattedForecastDate: () => (dt_txt: string) => ({
    date: new Date(dt_txt).toLocaleDateString(),
    time: new Date(dt_txt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
  }),
}));

describe('WeatherTimeSlot', () => {
  const mockWeatherData: WeatherData = {
    dt_txt: '2025-10-19 14:00:00',
    main: {
      temp: 25,
      feels_like: 26,
      temp_min: 22,
      temp_max: 28,
      pressure: 1012,
      sea_level: 1015,
      grnd_level: 1010,
      humidity: 60,
      temp_kf: 0,
    },
    weather: [
      {
        id: 800,
        main: 'Clear',
        description: 'clear sky',
        icon: '01d',
      },
    ],
    wind: {
      speed: 5,
      deg: 180,
      gust: 7,
    },
    predictions: ['Sunny', 'Warm'],
  };

  it('renders TimeSlotHeader with correct props', () => {
    render(<WeatherTimeSlot weatherData={mockWeatherData} index={0} />);

    const header = screen.getByTestId('time-slot-header');
    expect(header).toBeInTheDocument();
    expect(header.textContent).toContain('01d'); // icon
    expect(header.textContent).toContain('25'); // temp
    expect(header.textContent).toContain('26'); // feelsLike
  });

  it('renders WeatherDetails with correct description', () => {
    render(<WeatherTimeSlot weatherData={mockWeatherData} index={0} />);
    const details = screen.getByTestId('weather-details');
    expect(details).toBeInTheDocument();
    expect(details.textContent).toBe('clear sky');
  });

  it('renders PredictionsSection with correct predictions', () => {
    render(<WeatherTimeSlot weatherData={mockWeatherData} index={0} />);
    const predictions = screen.getByTestId('predictions-section');
    expect(predictions).toBeInTheDocument();
    expect(predictions.textContent).toBe('Sunny,Warm');
  });

  it('renders without crashing', () => {
    const { container } = render(<WeatherTimeSlot weatherData={mockWeatherData} index={0} />);
    expect(container.firstChild).toBeInTheDocument();
  });
});
