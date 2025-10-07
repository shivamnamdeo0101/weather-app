package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.dto.CustomResponse;
import com.shivam.weather_cache.exception.BadRequestException;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.shivam.weather_cache.utils.CityUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather-cache")
public class WeatherCacheController {
    private final WeatherCacheService cacheService;

    public WeatherCacheController(WeatherCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Operation(
        summary = "Get 3-hour weather forecast for a city",
        description = "Fetches 3-hour weather forecast data for the specified city. Returns cached payload when available and includes an X-Cache header indicating HIT or MISS."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Forecast fetched successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = java.util.Map.class),
                examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Forecast fetched successfully\",\"data\":[{\"city\":\"Indore\",\"timestamp\":1690000000,\"temperature\":30.5,\"weather\":\"Clouds\"}]}"))),
        @ApiResponse(responseCode = "400", description = "Bad request - missing, empty, or invalid city",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"City must contain only English letters, spaces or hyphens\",\"data\":null}"))),
        @ApiResponse(responseCode = "401", description = "OpenWeather API unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"OpenWeather API unauthorized\",\"data\":null}"))),
        @ApiResponse(responseCode = "404", description = "City not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"City not found\",\"data\":null}"))),
        @ApiResponse(responseCode = "429", description = "Too many requests",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Too many requests\",\"data\":null}"))),
        @ApiResponse(responseCode = "502", description = "Upstream service unavailable",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Upstream service unavailable\",\"data\":null}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Internal server error\",\"data\":null}")))
    })
    @GetMapping("/forecast")
    public ResponseEntity<CustomResponse<Object>> getWeather(@RequestParam(name = "city", required = true) String city) {
        if (city == null) {
            throw new BadRequestException("City parameter is required");
        }
        String trimmed = CityUtils.validateAndTrimCity(city);

        CacheResult result = cacheService.getWeather(trimmed);
        String headerValue = result.isCacheHit() ? AppConstants.Headers.CACHE_HIT : AppConstants.Headers.CACHE_MISS;

        return ResponseEntity.ok()
                .header(AppConstants.Headers.X_CACHE, headerValue)
                .body(new CustomResponse<>(true, AppConstants.Messages.FORECAST_SUCCESS, result.getData()));
    }

    
}