package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.ForecastResponseDTO;
import com.shivam.weather_svc.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.cnt}")
    private String cnt;

    @Value("${weather.api.units}")
    private String units;

    private final RestTemplate restTemplate = new RestTemplate();
    private final WeatherPredictionService predictionService;

    public WeatherService(WeatherPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public List<ForecastItemDTO> getThreeHourForecast(String cityName) {
        try {
            if (cityName == null || cityName.trim().isEmpty()) {
                throw new ExternalApiException("City name cannot be empty.", HttpStatus.BAD_REQUEST);
            }

            log.info("Fetching weather data for city: {}", cityName);
            String url = String.format("%s?q=%s&cnt=%s&units=%s&appid=%s",
                    apiUrl, cityName.trim(), cnt, units, apiKey);

            ForecastResponseDTO response = restTemplate.getForObject(url, ForecastResponseDTO.class);

            if (response == null || response.getList() == null) {
                log.warn("Empty or null response received from weather API for city: {}", cityName);
                return Collections.emptyList();
            }

            // Add business predictions
            for (ForecastItemDTO item : response.getList()) {
                item.setPredictions(predictionService.generatePredictions(item));
            }

            return response.getList();

        } catch (HttpClientErrorException e) {
            HttpStatusCode status = e.getStatusCode();
            String errorBody = e.getResponseBodyAsString();
            log.error("Weather API HTTP Error ({}): {}", status, errorBody);

            if (status == HttpStatus.NOT_FOUND) {
                throw new ExternalApiException("City not found: " + cityName, HttpStatus.NOT_FOUND);
            } else if (status == HttpStatus.BAD_REQUEST) {
                throw new ExternalApiException("Bad request: Invalid city name or malformed query parameters.", HttpStatus.BAD_REQUEST);
            } else if (status == HttpStatus.UNAUTHORIZED) {
                throw new ExternalApiException("Unauthorized: Invalid or missing API key.", HttpStatus.UNAUTHORIZED);
            }

            throw new ExternalApiException("Weather API returned an error: " + errorBody, status);

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Weather API request timed out for city: {}", cityName);
                throw new ExternalApiException("Weather service timed out. Please try again later.", HttpStatus.GATEWAY_TIMEOUT);
            }
            log.error("Weather API service unreachable: {}", e.getMessage());
            throw new ExternalApiException("Weather service is currently unreachable. Please try again later.", HttpStatus.BAD_GATEWAY);

        } catch (ExternalApiException e) {
            throw e; // rethrow cleanly for handler
        } catch (Exception e) {
            log.error("Unexpected error fetching weather data for city {}: {}", cityName, e.getMessage(), e);
            throw new ExternalApiException("Unexpected internal error while fetching weather data.", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Completed fetching weather data for city: {}", cityName);
        }
    }
}
