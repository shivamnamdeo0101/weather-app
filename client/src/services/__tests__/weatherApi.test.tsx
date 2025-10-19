import { WeatherApiService, WeatherError } from '../weatherApi';
import { Logger } from '@/utils/LogLevel';

jest.mock('@/utils/LogLevel', () => ({
  Logger: {
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn(),
  },
}));

global.fetch = jest.fn();

describe('WeatherApiService', () => {
  const city = 'London';
  const url = `http://localhost:8081/api/weather-cache/forecast?city=${encodeURIComponent(city)}`;

  beforeEach(() => {
    jest.clearAllMocks();
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
    expect(Logger.info).toHaveBeenCalledWith('HTTP error', 404, { message: 'City not found' });
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

    await expect(WeatherApiService.getForecast(city)).rejects.toThrow('Network fail');
    expect(Logger.error).toHaveBeenCalledWith(
      expect.stringContaining('All retry attempts failed'),
      expect.any(Error)
    );
  });
});
