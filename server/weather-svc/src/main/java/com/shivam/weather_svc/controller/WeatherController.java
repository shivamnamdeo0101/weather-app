package com.shivam.weather_svc.controller;

import com.shivam.weather_svc.dto.CustomResponse;
import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.utils.SlidingWindowRateLimiter;
import com.shivam.weather_svc.service.WeatherService;
import com.shivam.weather_svc.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing weather forecast APIs.
 */
@RestController
@RequestMapping("/api/weather-svc")
@Slf4j
public class WeatherController {

    private final WeatherService WeatherService;
    private final SlidingWindowRateLimiter rateLimiter;

    public WeatherController(WeatherService WeatherService, SlidingWindowRateLimiter rateLimiter) {
        this.WeatherService = WeatherService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Retrieves 3-hour weather forecast for a specific city.
     *
     * @param city the name of the city
     * @return standardized API response containing the forecast list
     */
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
    public ResponseEntity<CustomResponse<List<ForecastItemDTO>>> getForecast(@RequestParam String city) {
        log.info("Incoming request for weather forecast: city={}", city);

        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty.");
        }

        // Rate limiting
        if (!rateLimiter.tryConsume()) {
            log.warn("Rate limit exceeded for forecast API request");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new CustomResponse<>(false, AppConstants.Messages.TOO_MANY_REQUEST, null));
        }

        List<ForecastItemDTO> forecast = WeatherService.getThreeHourForecast(city);

        if (forecast == null || forecast.isEmpty()) {
            log.warn("No forecast data found for city: {}", city);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomResponse<>(false, AppConstants.Messages.CITY_NOT_FOUND + " " + city, null));
        }

        log.info("Forecast retrieved successfully for city: {}", city);
        return ResponseEntity.ok(
                new CustomResponse<>(true, AppConstants.Messages.FORECAST_SUCCESS, forecast)
        );
    }
}
