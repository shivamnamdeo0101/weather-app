package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.ForecastResponseDTO;
import com.shivam.weather_svc.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private final WeatherPredictionService predictionService;


    public WeatherService(WeatherPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public List<ForecastItemDTO> getThreeHourForecast(String cityName) {
        try {
            log.info("Fetching weather data for city: {}", cityName);
            String url = apiUrl + "?q=" + cityName + "&cnt=10&units=metric&appid=" + apiKey;

            ForecastResponseDTO response = restTemplate.getForObject(url, ForecastResponseDTO.class);

            if (response == null || response.getList() == null) {
                return Collections.emptyList();
            }

            // Add predictions using the dedicated business logic service
            for (ForecastItemDTO item : response.getList()) {
                item.setPredictions(predictionService.generatePredictions(item));
            }

            return response.getList();

        } catch (HttpClientErrorException e) {
            log.error("HTTP Error when calling weather API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Map API response code to HttpStatus
            HttpStatusCode status = e.getStatusCode();
            String message = "Weather API call failed: " + e.getMessage();
            throw new ExternalApiException(message, status);
        } catch (Exception e) {
            log.error("Unexpected error fetching weather data for city {}: {}", cityName, e.getMessage());
        } finally {
            log.info("Completed fetching weather data for city: {}", cityName);
        }
        return Collections.emptyList();
    }
}
