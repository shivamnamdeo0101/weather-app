import React from 'react';
import { render } from '@testing-library/react';
import { useGroupedForecast } from '../useGroupedForecast';
import { WeatherData } from '@/types/weather';

function HookTester({ weatherData }: { weatherData: WeatherData[] }) {
  const grouped = useGroupedForecast(weatherData);

  return (
    <div>
      {grouped.map((group) => (
        <div key={group.date} data-date={group.date} data-count={group.items.length}></div>
      ))}
    </div>
  );
}

describe('useGroupedForecast', () => {
  const mockWeatherData: WeatherData[] = [
    {
      dt_txt: '2025-10-19 14:00:00',
      main: { temp: 25, feels_like: 26, temp_min: 22, temp_max: 28, pressure: 1012, sea_level: 1012, grnd_level: 1009, humidity: 60, temp_kf: 0 },
      weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
      wind: { speed: 5, deg: 180, gust: 7 },
      predictions: ['sunny'],
    },
    {
      dt_txt: '2025-10-19 17:00:00',
      main: { temp: 22, feels_like: 23, temp_min: 20, temp_max: 24, pressure: 1010, sea_level: 1010, grnd_level: 1007, humidity: 55, temp_kf: 0 },
      weather: [{ id: 801, main: 'Clouds', description: 'few clouds', icon: '02d' }],
      wind: { speed: 3, deg: 200, gust: 5 },
      predictions: ['cloudy'],
    },
    {
      dt_txt: '2025-10-20 10:00:00',
      main: { temp: 21, feels_like: 21, temp_min: 19, temp_max: 23, pressure: 1015, sea_level: 1015, grnd_level: 1012, humidity: 50, temp_kf: 0 },
      weather: [{ id: 802, main: 'Clouds', description: 'scattered clouds', icon: '03d' }],
      wind: { speed: 4, deg: 150, gust: 6 },
      predictions: ['cloudy'],
    },
  ];

  it('groups weather data by date', () => {
    const { container } = render(<HookTester weatherData={mockWeatherData} />);
    const divs = Array.from(container.querySelectorAll('div[data-date]')) as HTMLElement[];

    expect(divs.length).toBe(2);
    expect(divs[0].dataset.count).toBe('2');
    expect(divs[1].dataset.count).toBe('1');
  });

  it('returns empty array for empty input', () => {
    const { container } = render(<HookTester weatherData={[]} />);
    const divs = Array.from(container.querySelectorAll('div[data-date]')) as HTMLElement[];
    expect(divs.length).toBe(0);
  });

  it('preserves original order of items', () => {
    const { container } = render(<HookTester weatherData={mockWeatherData} />);
    const divs = Array.from(container.querySelectorAll('div[data-date]')) as HTMLElement[];
    expect(divs[0].dataset.count).toBe('2');
    expect(divs[1].dataset.count).toBe('1');
  });
});
