import { renderHook, act } from '@testing-library/react';
import useWeatherSearch from '../useWeatherSearch';
import { WeatherApiService } from '@/services/weatherApi';
import { WeatherData } from '@/types/weather';

jest.mock('@/services/weatherApi');
const mockedGetForecast = WeatherApiService.getForecast as jest.Mock;

describe('useWeatherSearch hook', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  it('should initialize with default state', () => {
    const { result } = renderHook(() => useWeatherSearch());
    expect(result.current.weatherData).toEqual([]);
    expect(result.current.currentCity).toBe('');
    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(result.current.searchingCity).toBe('');
  });   


  
  it('should handle errors correctly', async () => {
    mockedGetForecast.mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useWeatherSearch());
    await act(async () => result.current.handleSearch('Delhi'));

    expect(result.current.weatherData).toEqual([]);
    expect(result.current.error).toBe('Network error');
    expect(result.current.isLoading).toBe(false);
  });
});
