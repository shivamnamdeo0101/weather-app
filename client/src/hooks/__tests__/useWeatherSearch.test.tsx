import { renderHook, act } from '@testing-library/react';
import useWeatherSearch from '../useWeatherSearch';
import { WeatherApiService, WeatherError } from '@/services/weatherApi';
import { WeatherData } from '@/types/weather';

// Mock the module but preserve the WeatherError class
jest.mock('@/services/weatherApi', () => {
  const actual = jest.requireActual('@/services/weatherApi');
  return {
    ...actual,
    WeatherApiService: {
      ...actual.WeatherApiService,
      getForecast: jest.fn(),
    },
  };
});
const mockedGetForecast = WeatherApiService.getForecast as jest.Mock;

describe('useWeatherSearch hook', () => {
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
      predictions: ['sunny'],
    },
  ];

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

  it('should fetch and set weather data successfully', async () => {
    const mockResponse = {
      success: true,
      message: 'Success',
      data: mockWeatherData,
    };

    mockedGetForecast.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.weatherData).toEqual(mockWeatherData);
    expect(result.current.currentCity).toBe('London');
    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should handle success response without data', async () => {
    const mockResponse = {
      success: false,
      message: 'No data available',
      data: null,
    };

    mockedGetForecast.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.weatherData).toEqual([]);
    expect(result.current.error).toBe('No data available');
  });

  it('should handle WeatherError with 404 status', async () => {
    const weatherError = new WeatherError({
      message: 'City not found: London',
      status: 404,
    });
    
    mockedGetForecast.mockRejectedValue(weatherError);

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.weatherData).toEqual([]);
    expect(result.current.error).toContain('London');
    expect(result.current.error).toContain('not found');
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle WeatherError with 400 status', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Invalid request',
        status: 400,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('InvalidCity');
    });

    expect(result.current.error).toContain('Invalid request');
  });

  it('should handle WeatherError with 500 status', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Server error',
        status: 500,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('Server error');
  });

  it('should handle WeatherError with 503 status', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Service unavailable',
        status: 503,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('Service unavailable');
  });

  it('should handle WeatherError without status (network error)', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Network connection failed',
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('Network connection failed');
  });

  it('should handle generic Error', async () => {
    const genericError = new Error('Generic error message');

    mockedGetForecast.mockRejectedValue(genericError);

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toBe('Generic error message');
  });

  it('should handle unknown error type', async () => {
    mockedGetForecast.mockRejectedValue('String error');

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toBe('An unexpected error occurred. Please try again.');
  });

  it('should set loading state during search', async () => {
    let resolvePromise: (value: { success: boolean; message: string; data: WeatherData[] | null }) => void;
    const promise = new Promise(resolve => {
      resolvePromise = resolve;
    });

    mockedGetForecast.mockReturnValue(promise);

    const { result } = renderHook(() => useWeatherSearch());

    act(() => {
      result.current.handleSearch('London');
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.searchingCity).toBe('London');

    await act(async () => {
      resolvePromise!({
        success: true,
        message: 'Success',
        data: mockWeatherData,
      });
      await promise;
    });

    expect(result.current.isLoading).toBe(false);
  });

  it('should clear error when starting new search', async () => {
    mockedGetForecast.mockRejectedValueOnce(new Error('First error'));
    mockedGetForecast.mockResolvedValueOnce({
      success: true,
      message: 'Success',
      data: mockWeatherData,
    });

    const { result } = renderHook(() => useWeatherSearch());

    // First search fails
    await act(async () => {
      await result.current.handleSearch('Invalid');
    });

    expect(result.current.error).toBeTruthy();

    // Second search succeeds
    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toBeNull();
    expect(result.current.weatherData).toEqual(mockWeatherData);
  });

  it('should support offline mode parameter', async () => {
    const mockResponse = {
      success: true,
      message: 'Success',
      data: mockWeatherData,
    };

    mockedGetForecast.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London', true);
    });

    expect(mockedGetForecast).toHaveBeenCalledWith('London', true);
    expect(result.current.weatherData).toEqual(mockWeatherData);
  });

  it('should clear error when clearError is called', async () => {
    mockedGetForecast.mockRejectedValueOnce(new Error('Test error'));

    const { result } = renderHook(() => useWeatherSearch());

    // First set an error
    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toBeTruthy();

    // Then clear it
    act(() => {
      result.current.clearError();
    });

    expect(result.current.error).toBeNull();
    expect(result.current.searchingCity).toBe('');
  });

  it('should handle 404 error without "not found" in message', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Resource unavailable',
        status: 404,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    // Should show a message about no forecast found
    expect(result.current.error).toBeTruthy();
  });

  it('should handle 401 status', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Unauthorized',
        status: 401,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('Unauthorized');
  });

  it('should handle 429 status', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Rate limit exceeded',
        status: 429,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('Rate limit');
  });

  it('should handle 502 status', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Bad gateway',
        status: 502,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toBeTruthy();
  });

  it('should handle WeatherError with empty message', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: '',
        status: 404,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('London');
  });

  it('should handle WeatherError with default status code', async () => {
    mockedGetForecast.mockRejectedValue(
      new WeatherError({
        message: 'Unknown error',
        status: 418,
      })
    );

    const { result } = renderHook(() => useWeatherSearch());

    await act(async () => {
      await result.current.handleSearch('London');
    });

    expect(result.current.error).toContain('Unknown error');
  });
});
