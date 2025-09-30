package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * WeatherService: Fetches per-3-hour weather forecast with predictions.
 */
@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches forecast from API and returns per-3-hour forecast with predictions.
     *
     * @param cityName Name of the city
     * @return List of ForecastItemDTO
     */
    public List<ForecastItemDTO> getThreeHourForecast(String cityName) {
        try {
            String url = apiUrl + "?q=" + cityName + "&cnt=10&units=metric&appid=" + apiKey;
            ForecastResponseDTO response = restTemplate.getForObject(url, ForecastResponseDTO.class);

            if (response == null || response.getList() == null) {
                return Collections.emptyList();
            }

            // Add predictions for each 3-hour forecast
            for (ForecastItemDTO item : response.getList()) {
                item.setPredictions(generatePredictionsForItem(item));
            }

            return response.getList();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Generates predictions for a single forecast item.
     *
     * @param item ForecastItemDTO
     * @return List of predictions
     */
    private List<String> generatePredictionsForItem(ForecastItemDTO item) {
        List<String> predictions = new ArrayList<>();

        for (WeatherDTO weather : item.getWeather()) {
            if (weather.getMain().equalsIgnoreCase("Rain")) {
                predictions.add("Carry umbrella");
            }
            if (weather.getMain().equalsIgnoreCase("Thunderstorm")) {
                predictions.add("Don't step out! A Storm is brewing!");
            }
        }
        if (item.getMain().getTemp_max() > 40) { // >40°C
            predictions.add("Use sunscreen lotion");
        }
        if (item.getWind().getSpeed() > 10) { // >10 mph
            predictions.add("It's too windy, watch out!");
        }

        return predictions;
    }
}
