import { OfflineCacheService } from '../offlineCache';
import { WeatherApiResponse } from '@/types/weather';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
    get length() {
      return Object.keys(store).length;
    },
    key: (index: number) => {
      const keys = Object.keys(store);
      return keys[index] || null;
    },
  };
})();

Object.defineProperty(global, 'localStorage', {
  value: localStorageMock,
});

describe('OfflineCacheService', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  const mockWeatherData: WeatherApiResponse = {
    success: true,
    message: 'Success',
    data: [
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
        predictions: ['sunny', 'warm'],
      },
    ],
  };

  describe('set and get', () => {
    it('should store and retrieve weather data', () => {
      OfflineCacheService.set('London', mockWeatherData);
      const cached = OfflineCacheService.get('London');

      expect(cached).toEqual(mockWeatherData);
    });

    it('should return null for non-existent city', () => {
      const cached = OfflineCacheService.get('NonExistent');
      expect(cached).toBeNull();
    });

    it('should handle case-insensitive city names', () => {
      OfflineCacheService.set('LONDON', mockWeatherData);
      const cached = OfflineCacheService.get('london');
      expect(cached).toEqual(mockWeatherData);
    });

    it('should trim city names', () => {
      OfflineCacheService.set('  London  ', mockWeatherData);
      const cached = OfflineCacheService.get('London');
      expect(cached).toEqual(mockWeatherData);
    });
  });

  describe('has', () => {
    it('should return true if city exists in cache', () => {
      OfflineCacheService.set('London', mockWeatherData);
      expect(OfflineCacheService.has('London')).toBe(true);
    });

    it('should return false if city does not exist', () => {
      expect(OfflineCacheService.has('NonExistent')).toBe(false);
    });
  });

  describe('remove', () => {
    it('should remove cached data for a city', () => {
      OfflineCacheService.set('London', mockWeatherData);
      expect(OfflineCacheService.has('London')).toBe(true);

      OfflineCacheService.remove('London');
      expect(OfflineCacheService.has('London')).toBe(false);
    });
  });

  describe('expiry', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should return null for expired cache entries', () => {
      OfflineCacheService.set('London', mockWeatherData);
      
      // Fast-forward 25 hours (cache expires after 24 hours)
      jest.advanceTimersByTime(25 * 60 * 60 * 1000);

      const cached = OfflineCacheService.get('London');
      expect(cached).toBeNull();
    });

    it('should return data for non-expired cache entries', () => {
      OfflineCacheService.set('London', mockWeatherData);
      
      // Fast-forward 23 hours (within 24 hour expiry)
      jest.advanceTimersByTime(23 * 60 * 60 * 1000);

      const cached = OfflineCacheService.get('London');
      expect(cached).toEqual(mockWeatherData);
    });
  });

  describe('clearExpiredEntries', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should remove expired entries', () => {
      OfflineCacheService.set('London', mockWeatherData);
      OfflineCacheService.set('Paris', mockWeatherData);

      // Fast-forward 25 hours
      jest.advanceTimersByTime(25 * 60 * 60 * 1000);

      OfflineCacheService.clearExpiredEntries();

      expect(OfflineCacheService.has('London')).toBe(false);
      expect(OfflineCacheService.has('Paris')).toBe(false);
    });

    it('should keep non-expired entries', () => {
      OfflineCacheService.set('London', mockWeatherData);
      OfflineCacheService.set('Paris', mockWeatherData);

      // Fast-forward 23 hours
      jest.advanceTimersByTime(23 * 60 * 60 * 1000);

      OfflineCacheService.clearExpiredEntries();

      expect(OfflineCacheService.has('London')).toBe(true);
      expect(OfflineCacheService.has('Paris')).toBe(true);
    });

    it('should handle invalid cache entries', () => {
      // Manually add invalid entry
      localStorage.setItem('weather_cache_invalid', 'invalid json');

      // Should not throw and should remove invalid entries
      expect(() => OfflineCacheService.clearExpiredEntries()).not.toThrow();
      expect(localStorage.getItem('weather_cache_invalid')).toBeNull();
    });
  });

  describe('clearAll', () => {
    it('should remove all cached weather data', () => {
      OfflineCacheService.set('London', mockWeatherData);
      OfflineCacheService.set('Paris', mockWeatherData);
      OfflineCacheService.set('Tokyo', mockWeatherData);

      expect(OfflineCacheService.has('London')).toBe(true);
      expect(OfflineCacheService.has('Paris')).toBe(true);
      expect(OfflineCacheService.has('Tokyo')).toBe(true);

      OfflineCacheService.clearAll();

      expect(OfflineCacheService.has('London')).toBe(false);
      expect(OfflineCacheService.has('Paris')).toBe(false);
      expect(OfflineCacheService.has('Tokyo')).toBe(false);
    });

    it('should not affect non-weather cache entries', () => {
      localStorage.setItem('other_key', 'other_value');
      OfflineCacheService.set('London', mockWeatherData);

      OfflineCacheService.clearAll();

      expect(localStorage.getItem('other_key')).toBe('other_value');
      expect(OfflineCacheService.has('London')).toBe(false);
    });
  });

  describe('getCachedCities', () => {
    it('should return list of cached cities', () => {
      OfflineCacheService.set('London', mockWeatherData);
      OfflineCacheService.set('Paris', mockWeatherData);
      OfflineCacheService.set('Tokyo', mockWeatherData);

      const cities = OfflineCacheService.getCachedCities();
      
      expect(cities).toContain('london');
      expect(cities).toContain('paris');
      expect(cities).toContain('tokyo');
      expect(cities.length).toBe(3);
    });

    it('should return empty array when no cities cached', () => {
      const cities = OfflineCacheService.getCachedCities();
      expect(cities).toEqual([]);
    });

    it('should skip invalid cache entries', () => {
      OfflineCacheService.set('London', mockWeatherData);
      localStorage.setItem('weather_cache_invalid', 'invalid json');

      const cities = OfflineCacheService.getCachedCities();
      
      expect(cities).toContain('london');
      expect(cities.length).toBe(1);
    });
  });

  describe('error handling', () => {
    it('should handle localStorage errors gracefully in set', () => {
      // Simulate localStorage quota exceeded
      const originalSetItem = localStorage.setItem;
      localStorage.setItem = jest.fn(() => {
        throw new Error('QuotaExceededError');
      });

      expect(() => {
        OfflineCacheService.set('London', mockWeatherData);
      }).not.toThrow();

      localStorage.setItem = originalSetItem;
    });

    it('should handle localStorage errors gracefully in get', () => {
      // Simulate localStorage read error
      const originalGetItem = localStorage.getItem;
      localStorage.getItem = jest.fn(() => {
        throw new Error('Storage error');
      });

      const cached = OfflineCacheService.get('London');
      expect(cached).toBeNull();

      localStorage.getItem = originalGetItem;
    });
  });
});

