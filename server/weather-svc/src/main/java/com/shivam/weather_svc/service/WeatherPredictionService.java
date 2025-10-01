package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.WeatherDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WeatherPredictionService {

    /**
     * Generates weather-based predictions/recommendations for a given forecast item.
     *
     * @param item the ForecastItemDTO containing weather data
     * @return a list of prediction messages
     */
    public List<String> generatePredictions(ForecastItemDTO item) {
        List<String> predictions = new ArrayList<>();

        // Analyze weather conditions (Rain, Thunderstorm)
        for (WeatherDTO weather : item.getWeather()) {
            if ("Rain".equalsIgnoreCase(weather.getMain())) {
                predictions.add("Carry umbrella");
            }
            if ("Thunderstorm".equalsIgnoreCase(weather.getMain())) {
                predictions.add("Don't step out! A Storm is brewing!");
            }
        }

        // Check if temperature exceeds 40°C
        if (item.getMain().getTemp_max() > 40) {
            predictions.add("Use sunscreen lotion");
        }

        // Check if wind speed exceeds 10 mph
        if (item.getWind().getSpeed() > 10) {
            predictions.add("It's too windy, watch out!");
        }

        return predictions;
    }
}
