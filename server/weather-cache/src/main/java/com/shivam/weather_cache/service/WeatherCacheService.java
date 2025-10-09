package com.shivam.weather_cache.service;

import com.shivam.weather_cache.dto.CacheResult;

public interface WeatherCacheService {

    CacheResult getWeather(String city);
}
