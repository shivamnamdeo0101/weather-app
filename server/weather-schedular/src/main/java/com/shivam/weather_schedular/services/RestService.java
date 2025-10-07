package com.shivam.weather_schedular.services;

import com.shivam.weather_schedular.dto.CacheResult;

import java.util.Map;
import java.util.Set;

public interface RestService {
    Set<String>  getAllKeys(String key);
    Map<Object, Object> getMeta(String key);
    CacheResult getWeather(String key);
    void deleteKey(String key);
}
