package com.shivam.weather_svc.controller;

import com.shivam.weather_svc.dto.response.ApiResponse;
import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.service.WeatherService;
import com.shivam.weather_svc.utils.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller to expose weather forecast APIs
 */
@RestController
@RequestMapping("/weather")
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Get 3-hour forecast for a city
     * Example: /weather/forecast?city=London
     *
     * @param city name of the city
     * @return standardized API response containing forecast list
     */
    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<List<ForecastItemDTO>>> getForecast(@RequestParam String city) {
        List<ForecastItemDTO> forecast = weatherService.getThreeHourForecast(city);
        log.info("Fetching forecast for city: {}", city);
        if (forecast == null || forecast.isEmpty()) {
            log.info("Forecast not found for city: {}", city);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(true, AppConstants.Messages.FORECAST_NOT_FOUND + city, null));
        }
        log.info("Forecast data: {}", forecast);
        log.info("Forecast retrieved successfully for city: {}", city);
        return ResponseEntity.ok(
                new ApiResponse<>(true, AppConstants.Messages.FORECAST_SUCCESS, forecast)
        );
    }
}
