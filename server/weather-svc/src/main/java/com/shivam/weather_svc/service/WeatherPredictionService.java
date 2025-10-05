package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.WeatherDTO;
import com.shivam.weather_svc.utils.AppConstants;
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
            if (AppConstants.Weather.Conditions.RAIN.equalsIgnoreCase(weather.getMain())) {
                predictions.add(AppConstants.Weather.Predictions.CARRY_UMBRELLA);
            }
            if (AppConstants.Weather.Conditions.THUNDERSTORM.equalsIgnoreCase(weather.getMain())) {
                predictions.add(AppConstants.Weather.Predictions.STORM_WARNING);
            }
        }

        // Check if temperature exceeds configured max
        if (item.getMain().getTemp_max() > AppConstants.Weather.MAX_TEMP_THRESHOLD) {
            predictions.add(AppConstants.Weather.Predictions.USE_SUNSCREEN);
        }

        // Check if wind speed exceeds configured max
        if (item.getWind().getSpeed() > AppConstants.Weather.MAX_WIND_SPEED) {
            predictions.add(AppConstants.Weather.Predictions.TOO_WINDY);
        }

        return predictions;
    }
}
