package com.shivam.weather_cache.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CacheResult {
    private final Map<String, Object> data;
    private final boolean cacheHit;

    // Proper getter method for clarity
    public boolean isFromCache() {
        return cacheHit;
    }
}
