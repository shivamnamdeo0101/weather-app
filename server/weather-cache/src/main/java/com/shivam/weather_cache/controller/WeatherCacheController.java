package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.WeatherCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather-cache")
public class WeatherCacheController {
    private final WeatherCacheService cacheService;

    public WeatherCacheController(WeatherCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/forecast")
    public ResponseEntity<java.util.Map<String, Object>> getWeather(@RequestParam(name = "city", required = true) String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be empty");
        }

        CacheResult result = cacheService.getWeather(city.trim());
        String headerValue = result.isCacheHit() ? "HIT" : "MISS";

        return ResponseEntity.ok()
                .header("X-Cache", headerValue)
                .body(result.getData());
    }
}