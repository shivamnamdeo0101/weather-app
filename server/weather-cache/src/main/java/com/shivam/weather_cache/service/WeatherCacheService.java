package com.shivam.weather_cache.service;

import com.shivam.weather_cache.dto.CacheResult;

/**
 * WeatherCacheService defines caching and retrieval operations
 * for weather forecast data using Redis and an external Weather Service.
 *
 * <p>It abstracts the caching layer, ensuring clients can fetch
 * weather data efficiently while minimizing API calls to the
 * upstream Weather SVC.</p>
 */
public interface WeatherCacheService {

    /**
     * Retrieves weather data for the specified city.
     *
     * <p>This method first attempts to fetch cached data from Redis.
     * If the cache is missed, it fetches fresh data from the Weather SVC
     * and stores it in Redis with a configured TTL.</p>
     *
     * @param city the name of the city to fetch weather data for
     * @return a {@link CacheResult} containing the weather data and cache status
     * @throws IllegalArgumentException if the city parameter is null or blank
     * @throws com.shivam.weather_cache.exception.WeatherServiceException if the Weather SVC call fails
     */
    CacheResult getWeather(String city);
}
