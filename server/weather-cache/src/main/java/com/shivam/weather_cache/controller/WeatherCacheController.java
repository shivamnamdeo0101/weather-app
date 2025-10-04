package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.WeatherCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/weather-cache")
public class WeatherCacheController {

    @Autowired
    private WeatherCacheService cacheService;

    @GetMapping("/forecast")
    public ResponseEntity<Map<String,Object>> getWeather(@RequestParam String city) {
        CacheResult result = (CacheResult) cacheService.getWeather(city);

        String headerValue = result.isCacheHit() ? "HIT" : "MISS";

        return ResponseEntity.ok()
                .header("X-Cache", headerValue)
                .body(result.getData());
    }
}

