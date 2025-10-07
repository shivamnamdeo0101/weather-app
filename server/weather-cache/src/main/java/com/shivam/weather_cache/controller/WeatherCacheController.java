package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.dto.CustomResponse;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.utils.AppConstants;
import com.shivam.weather_cache.exception.BadRequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "Fetches 3-hour weather forecast data for the specified city."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = AppConstants.Messages.FORECAST_SUCCESS,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "400", description = AppConstants.Messages.BAD_REQUEST, content = @Content),
            @ApiResponse(responseCode = "401", description = AppConstants.Messages.OPENWEATHER_API_UNAUTHORIZED, content = @Content),
            @ApiResponse(responseCode = "404", description = AppConstants.Messages.CITY_NOT_FOUND, content = @Content),
            @ApiResponse(responseCode = "429", description = AppConstants.Messages.TOO_MANY_REQUEST, content = @Content),
            @ApiResponse(responseCode = "502", description = AppConstants.Messages.SERVICE_UNAVAILABLE, content = @Content),
            @ApiResponse(responseCode = "500", description = AppConstants.Messages.INTERNAL_SERVER_ERROR, content = @Content)
    })
    @GetMapping("/forecast")
    public ResponseEntity<CustomResponse<Object>> getWeather(
            @RequestParam(name = "city", required = true) String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new BadRequestException("City cannot be empty");
        }

        CacheResult result = cacheService.getWeather(city.trim());
        return ResponseEntity.ok(new CustomResponse<>(true,
                AppConstants.Messages.FORECAST_SUCCESS, result.getData()));
    }
}