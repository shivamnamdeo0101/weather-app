package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.service.WeatherCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather-cache")
@Slf4j
@RequiredArgsConstructor
public class WeatherCacheController {

    private final WeatherCacheService weatherCacheService;

    @GetMapping(value = "/forecast", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getForecast(@RequestParam String city) {
        return weatherCacheService.getForecast(city);
    }
}
