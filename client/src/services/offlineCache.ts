import { WeatherApiResponse } from '@/types/weather';
import { Logger } from '@/utils/LogLevel';

interface CachedWeatherData {
  data: WeatherApiResponse;
  timestamp: number;
  city: string;
}

const CACHE_PREFIX = 'weather_cache_';
const CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours

export class OfflineCacheService {
  /**
   * Get cache key for a city
   */
  private static getCacheKey(city: string): string {
    return `${CACHE_PREFIX}${city.toLowerCase().trim()}`;
  }

  /**
   * Store weather data in cache
   */
  static set(city: string, data: WeatherApiResponse): void {
    try {
      const cacheKey = this.getCacheKey(city);
      const cachedData: CachedWeatherData = {
        data,
        timestamp: Date.now(),
        city: city.toLowerCase().trim(),
      };
      localStorage.setItem(cacheKey, JSON.stringify(cachedData));
      Logger.info(`Cached weather data for city: ${city}`);
    } catch (error) {
      Logger.error('Failed to cache weather data', error);
      // If localStorage is full, try to clear old entries
      this.clearExpiredEntries();
      try {
        const cacheKey = this.getCacheKey(city);
        const cachedData: CachedWeatherData = {
          data,
          timestamp: Date.now(),
          city: city.toLowerCase().trim(),
        };
        localStorage.setItem(cacheKey, JSON.stringify(cachedData));
      } catch (retryError) {
        Logger.error('Failed to cache after cleanup', retryError);
      }
    }
  }

  /**
   * Get cached weather data for a city
   */
  static get(city: string): WeatherApiResponse | null {
    try {
      const cacheKey = this.getCacheKey(city);
      const cached = localStorage.getItem(cacheKey);
      
      if (!cached) {
        return null;
      }

      const cachedData: CachedWeatherData = JSON.parse(cached);
      const age = Date.now() - cachedData.timestamp;

      // Check if cache is expired
      if (age > CACHE_EXPIRY_MS) {
        Logger.info(`Cache expired for city: ${city} (age: ${Math.round(age / 1000 / 60)} minutes)`);
        localStorage.removeItem(cacheKey);
        return null;
      }

      Logger.info(`Retrieved cached weather data for city: ${city} (age: ${Math.round(age / 1000 / 60)} minutes)`);
      return cachedData.data;
    } catch (error) {
      Logger.error('Failed to retrieve cached weather data', error);
      return null;
    }
  }

  /**
   * Check if data exists in cache (without retrieving it)
   */
  static has(city: string): boolean {
    return this.get(city) !== null;
  }

  /**
   * Remove cached data for a specific city
   */
  static remove(city: string): void {
    try {
      const cacheKey = this.getCacheKey(city);
      localStorage.removeItem(cacheKey);
      Logger.info(`Removed cached weather data for city: ${city}`);
    } catch (error) {
      Logger.error('Failed to remove cached weather data', error);
    }
  }

  /**
   * Check if a cache entry is expired
   */
  private static isCacheEntryExpired(key: string): boolean {
    try {
      const cached = localStorage.getItem(key);
      if (!cached) {
        return false;
      }
      const cachedData: CachedWeatherData = JSON.parse(cached);
      const age = Date.now() - cachedData.timestamp;
      return age > CACHE_EXPIRY_MS;
    } catch (error) {
      // Invalid cache entry, consider it expired
      Logger.warn('Invalid cache entry found, marking as expired', error);
      return true;
    }
  }

  /**
   * Get all cache keys
   */
  private static getAllCacheKeys(): string[] {
    const keys: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key?.startsWith(CACHE_PREFIX)) {
        keys.push(key);
      }
    }
    return keys;
  }

  /**
   * Clear all expired cache entries
   */
  static clearExpiredEntries(): void {
    try {
      const keys = this.getAllCacheKeys();
      const keysToRemove: string[] = [];
      
      for (const key of keys) {
        if (this.isCacheEntryExpired(key)) {
          keysToRemove.push(key);
        }
      }

      for (const key of keysToRemove) {
        localStorage.removeItem(key);
      }
      
      if (keysToRemove.length > 0) {
        Logger.info(`Cleared ${keysToRemove.length} expired cache entries`);
      }
    } catch (error) {
      Logger.error('Failed to clear expired cache entries', error);
    }
  }

  /**
   * Clear all cached weather data
   */
  static clearAll(): void {
    try {
      const keysToRemove = this.getAllCacheKeys();

      for (const key of keysToRemove) {
        localStorage.removeItem(key);
      }
      Logger.info(`Cleared all cached weather data (${keysToRemove.length} entries)`);
    } catch (error) {
      Logger.error('Failed to clear all cached weather data', error);
    }
  }

  /**
   * Get all cached cities
   */
  static getCachedCities(): string[] {
    try {
      const cities: string[] = [];
      const keys = this.getAllCacheKeys();
      
      for (const key of keys) {
        try {
          const cached = localStorage.getItem(key);
          if (cached) {
            const cachedData: CachedWeatherData = JSON.parse(cached);
            cities.push(cachedData.city);
          }
        } catch (error) {
          // Skip invalid entries
          Logger.warn('Skipping invalid cache entry when getting cities', error);
        }
      }

      return cities;
    } catch (error) {
      Logger.error('Failed to get cached cities', error);
      return [];
    }
  }
}

