import { WeatherApiService, WeatherError } from '../weatherApi';
import { Logger } from '@/utils/LogLevel';
import { OfflineCacheService } from '../offlineCache';

jest.mock('@/utils/LogLevel', () => ({
  Logger: {
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn(),
  },
}));

jest.mock('../offlineCache');

global.fetch = jest.fn();

const mockedOfflineCacheService = OfflineCacheService as jest.Mocked<typeof OfflineCacheService>;

describe('WeatherApiService', () => {
  const city = 'London';
  const url = `http://localhost:8081/api/weather-cache/forecast?city=${encodeURIComponent(city)}`;

  beforeEach(() => {
    jest.clearAllMocks();
    mockedOfflineCacheService.get.mockReturnValue(null);
    mockedOfflineCacheService.set.mockImplementation(() => {});
  });

  it('should fetch and return weather data successfully', async () => {
    const mockData = { data: { temp: 30, condition: 'Sunny' } };

    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    });

    const res = await WeatherApiService.getForecast(city);
    expect(fetch).toHaveBeenCalledWith(
      url,
      expect.objectContaining({ method: 'GET' })
    );
    expect(res).toEqual(mockData);
  });

  it('should throw WeatherError on HTTP error', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 404,
      json: async () => ({ message: 'City not found' }),
    });

    await expect(WeatherApiService.getForecast(city)).rejects.toThrow(WeatherError);
    expect(Logger.info).toHaveBeenCalledWith('HTTP error 404:', 'City not found');
  });

  it('should retry failed requests and succeed after retries', async () => {
    const mockData = { data: { temp: 22 } };

    (fetch as jest.Mock)
      .mockRejectedValueOnce(new Error('Network error'))
      .mockRejectedValueOnce(new Error('Network error'))
      .mockResolvedValueOnce({
        ok: true,
        json: async () => mockData,
      });

    const result = await WeatherApiService.getForecast(city);
    expect(result).toEqual(mockData);
    expect(fetch).toHaveBeenCalledTimes(3);
    expect(Logger.warn).toHaveBeenCalledTimes(2);
  });

  it('should throw after max retries', async () => {
    (fetch as jest.Mock).mockRejectedValue(new Error('Network fail'));

    try {
      await WeatherApiService.getForecast(city);
      fail('Expected WeatherError to be thrown');
    } catch (error) {
      expect(error).toBeInstanceOf(WeatherError);
      expect((error as WeatherError).message).toContain('Failed to connect to weather service after 3 attempts');
    }
    
    expect(Logger.error).toHaveBeenCalledWith(
      expect.stringContaining('All 3 retry attempts failed'),
      expect.any(Error)
    );
  });

  it('should use cached data in offline mode', async () => {
    const mockCachedData = { 
      success: true, 
      message: 'Success', 
      data: [{ 
        dt_txt: '2024-01-01 12:00:00',
        main: { temp: 25, feels_like: 26, temp_min: 20, temp_max: 30, pressure: 1013, sea_level: 1013, grnd_level: 1013, humidity: 60, temp_kf: 0 },
        weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
        wind: { speed: 5, deg: 180, gust: 7 },
        predictions: ['sunny'],
      }] 
    };
    
    mockedOfflineCacheService.get.mockReturnValue(mockCachedData);

    const result = await WeatherApiService.getForecast(city, true);
    expect(result).toEqual(mockCachedData);
    expect(Logger.info).toHaveBeenCalledWith(expect.stringContaining('Using cached data'));
  });

  it('should throw error in offline mode when no cache available', async () => {
    mockedOfflineCacheService.get.mockReturnValue(null);

    try {
      await WeatherApiService.getForecast(city, true);
      fail('Expected WeatherError to be thrown');
    } catch (error) {
      expect(error).toBeInstanceOf(WeatherError);
      expect((error as WeatherError).message).toContain('No cached data available');
    }
  });

  it('should cache successful API response', async () => {
    const mockData = { 
      success: true, 
      message: 'Success', 
      data: [{ 
        dt_txt: '2024-01-01 12:00:00',
        main: { temp: 25, feels_like: 26, temp_min: 20, temp_max: 30, pressure: 1013, sea_level: 1013, grnd_level: 1013, humidity: 60, temp_kf: 0 },
        weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
        wind: { speed: 5, deg: 180, gust: 7 },
        predictions: ['sunny'],
      }] 
    };

    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    });

    await WeatherApiService.getForecast(city);
    
    expect(mockedOfflineCacheService.set).toHaveBeenCalledWith(city, mockData);
  });

  it('should fallback to cache when API fails with non-404 error', async () => {
    const mockCachedData = { 
      success: true, 
      message: 'Success', 
      data: [{ 
        dt_txt: '2024-01-01 12:00:00',
        main: { temp: 25, feels_like: 26, temp_min: 20, temp_max: 30, pressure: 1013, sea_level: 1013, grnd_level: 1013, humidity: 60, temp_kf: 0 },
        weather: [{ id: 800, main: 'Clear', description: 'clear sky', icon: '01d' }],
        wind: { speed: 5, deg: 180, gust: 7 },
        predictions: ['sunny'],
      }] 
    };

    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({ message: 'Server error' }),
    });

    mockedOfflineCacheService.get.mockReturnValue(mockCachedData);

    const result = await WeatherApiService.getForecast(city);
    expect(result).toEqual(mockCachedData);
    expect(Logger.info).toHaveBeenCalledWith(expect.stringContaining('Using cached data'));
  });

  it('should not fallback to cache for 404 errors', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 404,
      json: async () => ({ message: 'City not found' }),
    });

    mockedOfflineCacheService.get.mockReturnValue(null);

    await expect(WeatherApiService.getForecast(city)).rejects.toThrow(WeatherError);
    // Should not attempt to get from cache for 404
  });

  it('should handle AbortError timeout', async () => {
    const abortError = new Error('Request timeout');
    abortError.name = 'AbortError';
    (fetch as jest.Mock).mockRejectedValue(abortError);

    mockedOfflineCacheService.get.mockReturnValue(null);

    await expect(WeatherApiService.getForecast(city)).rejects.toThrow(WeatherError);
    // AbortError should be handled and converted to WeatherError with timeout message
    expect(Logger.warn).toHaveBeenCalled();
  });

  it('should handle invalid JSON response', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => {
        throw new Error('Invalid JSON');
      },
    });

    mockedOfflineCacheService.get.mockReturnValue(null);

    await expect(WeatherApiService.getForecast(city)).rejects.toThrow(WeatherError);
    expect(Logger.error).toHaveBeenCalledWith(expect.stringContaining('Failed to parse response'), expect.any(Error));
  });

  it('should handle error response without JSON body', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => {
        throw new Error('Not JSON');
      },
    });

    mockedOfflineCacheService.get.mockReturnValue(null);

    await expect(WeatherApiService.getForecast(city)).rejects.toThrow(WeatherError);
    expect(Logger.warn).toHaveBeenCalled();
  });

  it('should handle response without data property', async () => {
    const mockData = { success: true, message: 'Success' }; // No data property

    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    });

    const result = await WeatherApiService.getForecast(city);
    expect(result).toEqual(mockData);
    // Should not cache if data is missing
    expect(mockedOfflineCacheService.set).not.toHaveBeenCalled();
  });
});
