package com.shivam.weather_svc.controller;

import com.shivam.weather_svc.dto.CustomResponse;
import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.service.SlidingWindowRateLimiterService;
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
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/weather-svc")
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;
    private final SlidingWindowRateLimiterService rateLimiter;

    public WeatherController(WeatherService weatherService,
                             SlidingWindowRateLimiterService rateLimiter) {
        this.weatherService = weatherService;
        this.rateLimiter = rateLimiter;
    }

    @Operation(
            summary = "Get 3-hour weather forecast for a city (reactive)",
            description = "Fetches 3-hour weather forecast data for the specified city using non-blocking WebClient."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = AppConstants.Messages.FORECAST_SUCCESS,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ForecastItemDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = AppConstants.Messages.OPENWEATHER_API_UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = AppConstants.Messages.CITY_NOT_FOUND),
            @ApiResponse(responseCode = "429", description = AppConstants.Messages.TOO_MANY_REQUEST),
            @ApiResponse(responseCode = "500", description = AppConstants.Messages.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/forecast")
    public Mono<ResponseEntity<CustomResponse<List<ForecastItemDTO>>>> getForecastReactive(
            @RequestParam String city) {

        log.info("Fetching reactive forecast for city: {}", city);

        // ✅ Rate limiting
        if (!rateLimiter.tryConsume()) {
            log.warn("Rate limit exceeded for city: {}", city);
            return Mono.just(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(new CustomResponse<>(false, AppConstants.Messages.TOO_MANY_REQUEST, null))
            );
        }

        // ✅ Non-blocking call
        return weatherService.getThreeHourForecastReactive(city)
                .map(forecast -> {
                    if (forecast == null || forecast.isEmpty()) {
                        log.warn("No forecast data found for city: {}", city);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new CustomResponse<List<ForecastItemDTO>>( // 👈 Explicit generic
                                        false,
                                        AppConstants.Messages.FORECAST_NOT_FOUND + city,
                                        null
                                ));
                    }
                    log.info("Reactive forecast retrieved successfully for city: {}", city);
                    return ResponseEntity.ok(
                            new CustomResponse<List<ForecastItemDTO>>( // 👈 Explicit generic
                                    true,
                                    AppConstants.Messages.FORECAST_SUCCESS,
                                    forecast
                            )
                    );
                })
                .onErrorResume(e -> {
                    log.error("Error while fetching forecast reactively for {}: {}", city, e.getMessage());
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(new CustomResponse<List<ForecastItemDTO>>( // 👈 Explicit generic
                                            false,
                                            e.getMessage(),
                                            null
                                    ))
                    );
                });

    }
}
