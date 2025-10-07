package com.shivam.weather_schedular.services;

import com.shivam.weather_schedular.dto.CacheResult;

import java.util.Map;
import java.util.Set;

public class RestServiceImpl implements RestService{

    @Override
    public Set<String> getAllKeys(String key) {
        return Set.of();
    }

    @Override
    public Map<Object, Object> getMeta(String key) {
        return Map.of();
    }

    @Override
    public CacheResult getWeather(String key) {
        return null;
    }

    @Override
    public void deleteKey(String key) {

    }
}
