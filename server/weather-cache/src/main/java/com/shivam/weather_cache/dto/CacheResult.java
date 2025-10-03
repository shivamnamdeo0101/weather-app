package com.shivam.weather_cache.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class CacheResult {
    private final Map<String, Object> data;
    private final boolean cacheHit;

    public CacheResult(Map<String, Object> data, boolean cacheHit) {
        this.data = data;
        this.cacheHit = cacheHit;
    }

}
