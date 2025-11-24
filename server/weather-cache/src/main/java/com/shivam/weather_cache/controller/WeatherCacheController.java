package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.utils.AppConstants;
import com.shivam.weather_cache.utils.WeatherUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather-cache")
public class WeatherCacheController {

    private final WeatherCacheService cacheService;

    @Operation(
            summary = "Get 3-hour weather forecast for a city",
            description = "Fetches 3-hour weather forecast data for the specified city. Returns cached payload when available and includes an X-Cache header indicating HIT or MISS."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Forecast fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - missing or invalid city"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "429", description = "Too many requests"),
            @ApiResponse(responseCode = "502", description = "Upstream service unavailable"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/forecast")
    public ResponseEntity<java.util.Map<String, Object>> getWeather(@RequestParam(name = "city") String city) {
        String trimmed = WeatherUtils.validateAndTrimCity(city);

        CacheResult result = cacheService.getWeather(trimmed);
        String headerValue = result.isCacheHit() ? AppConstants.Headers.CACHE_HIT : AppConstants.Headers.CACHE_MISS;

        return ResponseEntity.ok()
                .header(AppConstants.Headers.X_CACHE, headerValue)
                .body(result.getData());
    }
}
