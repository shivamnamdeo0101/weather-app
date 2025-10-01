package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.*;

import com.shivam.weather_svc.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * WeatherService: Fetches per-3-hour weather forecast with predictions.
 */
@Service
@Slf4j
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
            log.info("Fetching weather data for city: {}", cityName);
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

        }catch (HttpClientErrorException e) {
            log.error("HTTP Error when calling weather API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalApiException(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching weather data for city {}: {}", cityName, e.getMessage());
        }finally {
            log.info("Completed fetching weather data for city: {}", cityName);
        }
        return Collections.emptyList();
    }

    /**
     * Generates weather-based predictions/recommendations for a given forecast item.
     * <p>
     * This method inspects various weather parameters such as condition, temperature,
     * and wind speed, and returns a list of human-readable recommendations.
     * </p>
     *
     * Rules applied:
     * <ul>
     *     <li>If weather condition is "Rain" → Suggest carrying an umbrella.</li>
     *     <li>If weather condition is "Thunderstorm" → Warn user not to step outside.</li>
     *     <li>If maximum temperature exceeds 40°C → Recommend using sunscreen lotion.</li>
     *     <li>If wind speed exceeds 10 mph → Warn about high wind.</li>
     * </ul>
     *
     * @param item the {@link ForecastItemDTO} containing weather data
     * @return a list of prediction messages relevant to the given forecast
     */
    private List<String> generatePredictionsForItem(ForecastItemDTO item) {
        List<String> predictions = new ArrayList<>();

        // Analyze weather conditions (e.g., Rain, Thunderstorm)
        for (WeatherDTO weather : item.getWeather()) {
            if (weather.getMain().equalsIgnoreCase("Rain")) {
                predictions.add("Carry umbrella");
            }
            if (weather.getMain().equalsIgnoreCase("Thunderstorm")) {
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
