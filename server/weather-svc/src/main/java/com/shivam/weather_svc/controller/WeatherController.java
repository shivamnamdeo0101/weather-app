package com.shivam.weather_svc.controller;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller to expose weather forecast APIs
 */
@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Get 3-hour forecast for a city
     * Example: /weather/forecast?city=London
     */
    @GetMapping("/forecast")
    public List<ForecastItemDTO> getForecast(@RequestParam String city) {
        return weatherService.getThreeHourForecast(city);
    }
}
