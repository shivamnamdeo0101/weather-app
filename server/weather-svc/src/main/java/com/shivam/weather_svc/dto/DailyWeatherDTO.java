package com.shivam.weather_svc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO representing aggregated weather forecast for a single day
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyWeatherDTO {
    private String date;             // Date in yyyy-MM-dd
    private double minTemp;          // Minimum temperature for the day
    private double maxTemp;          // Maximum temperature for the day
    private String mainWeather;      // Most frequent weather condition (Clear, Clouds, etc.)
    private double maxWind;          // Maximum wind speed
    private List<String> predictions;// Predictions based on rules (umbrella, sunscreen, wind, thunderstorm)
}
