package com.shivam.weather_cache.utils;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherSvcClient {

    private final RestTemplate restTemplate;

    @Value("${weather.svc.url}")
    private String svcUrl;

    /**
     * Fetches weather data from external service with resilience.
     * Uses retry and circuit breaker to handle transient failures.
     */
    @Retry(name = "weatherServiceRetry", fallbackMethod = "fallbackWeatherData")
    @CircuitBreaker(name = "weatherServiceCircuit", fallbackMethod = "fallbackWeatherData")
    public Map<String, Object> fetchWeatherData(String city) {
        try {
            String url = UriComponentsBuilder.fromUriString(svcUrl)
                    .queryParam("city", city)
                    .toUriString();

            log.info("Calling Weather SVC: {} for {}", url, city);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            log.info("Weather SVC response status: {}", response.getStatusCode());
            return response.getBody();

        } catch (HttpStatusCodeException httpEx) {
            log.error("Weather SVC HTTP error {} for {}: {}", httpEx.getStatusCode(), city, httpEx.getMessage());
            throw httpEx;

        } catch (Exception ex) {
            log.error("Weather SVC network/timeout error for {}: {}", city, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Fallback method in case of failures.
     */
    public Map<String, Object> fallbackWeatherData(String city, Throwable ex) {
        log.warn("Weather service unavailable for {}. Returning empty data. Cause: {}", city, ex.toString());
        return Collections.emptyMap();
    }
}
