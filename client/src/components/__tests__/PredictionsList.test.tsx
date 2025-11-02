import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import PredictionsList from '../PredictionsList';
import { WeatherData } from '@/types/weather';

describe('PredictionsList', () => {
  const mockWeatherData: WeatherData[] = [
    {
      dt_txt: '2024-01-01 12:00:00',
      main: { 
        temp: 25, 
        feels_like: 26, 
        temp_min: 20, 
        temp_max: 30, 
        pressure: 1013, 
        sea_level: 1013, 
        grnd_level: 1013, 
        humidity: 60, 
        temp_kf: 0 
      },
      weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
      wind: { speed: 5, deg: 180, gust: 7 },
      predictions: ['Sunny day', 'Clear skies'],
    },
  ];

  it('renders empty state when no predictions', () => {
    const emptyData: WeatherData[] = [{
      dt_txt: '2024-01-01 12:00:00',
      main: { temp: 25, feels_like: 26, temp_min: 20, temp_max: 30, pressure: 1013, sea_level: 1013, grnd_level: 1013, humidity: 60, temp_kf: 0 },
      weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
      wind: { speed: 5, deg: 180, gust: 7 },
      predictions: [],
    }];

    render(<PredictionsList weatherData={emptyData} />);
    
    expect(screen.getByText('No Predictions Available')).toBeInTheDocument();
  });

  it('renders predictions with correct count', () => {
    render(<PredictionsList weatherData={mockWeatherData} />);
    
    expect(screen.getByText('Weather Predictions')).toBeInTheDocument();
    expect(screen.getByText('Sunny day')).toBeInTheDocument();
    expect(screen.getByText('Clear skies')).toBeInTheDocument();
  });

  it('renders single prediction', () => {
    const singlePrediction: WeatherData[] = [{
      ...mockWeatherData[0],
      predictions: ['Only one prediction'],
    }];

    render(<PredictionsList weatherData={singlePrediction} />);
    
    expect(screen.getByText('Only one prediction')).toBeInTheDocument();
  });

  it('renders weather details correctly', () => {
    render(<PredictionsList weatherData={mockWeatherData} />);
    
    expect(screen.getByText('25°C')).toBeInTheDocument();
    expect(screen.getByText('clear sky')).toBeInTheDocument();
    expect(screen.getAllByText(/60% humidity/).length).toBeGreaterThan(0);
    expect(screen.getByText(/Wind: 5 m\/s/)).toBeInTheDocument();
    expect(screen.getByText(/Pressure: 1013 hPa/)).toBeInTheDocument();
  });

  it('filters out items without predictions', () => {
    const mixedData: WeatherData[] = [
      mockWeatherData[0],
      {
        ...mockWeatherData[0],
        dt_txt: '2024-01-02 12:00:00',
        predictions: [],
      },
      {
        ...mockWeatherData[0],
        dt_txt: '2024-01-03 12:00:00',
        predictions: ['Rain expected'],
      },
    ];

    render(<PredictionsList weatherData={mixedData} />);
    
    expect(screen.getByText('Sunny day')).toBeInTheDocument();
    expect(screen.getByText('Rain expected')).toBeInTheDocument();
    expect(screen.queryByText('No Predictions Available')).not.toBeInTheDocument();
  });

  it('renders multiple items with predictions', () => {
    const multipleItems: WeatherData[] = [
      mockWeatherData[0],
      {
        ...mockWeatherData[0],
        dt_txt: '2024-01-02 14:00:00',
        main: { ...mockWeatherData[0].main, temp: 20 },
        predictions: ['Cloudy weather'],
      },
    ];

    render(<PredictionsList weatherData={multipleItems} />);
    
    expect(screen.getByText('25°C')).toBeInTheDocument();
    expect(screen.getByText('20°C')).toBeInTheDocument();
    expect(screen.getByText('Cloudy weather')).toBeInTheDocument();
  });
});

