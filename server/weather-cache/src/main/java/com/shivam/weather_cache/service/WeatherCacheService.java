package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.Map;

@Service
@Slf4j
public class WeatherCacheService {

    @Autowired
    private GenericRedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Value("${spring.redis.ttl}")
    private long cacheTTL;

    @Value("${weather.svc.url}")
    private String svcUrl;

    public WeatherCacheService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CacheResult getWeather(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City parameter cannot be empty");
        }

        String key = "weather:" + city.toLowerCase();
        log.info("Fetching weather for city: {}", city);

        Object cached = redisService.get(key);
        if (cached != null) {
            Map<String, Object> cachedMap = objectMapper.convertValue(cached, new TypeReference<>() {});
            log.info("Cache HIT for city: {}", city);
            return new CacheResult(cachedMap, true);
        }

        log.info("Cache MISS for city: {}. Calling Weather SVC: {}", city, svcUrl);

        try {
            String url = svcUrl + "?city=" + city;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> data = response.getBody();
            if (data == null || data.isEmpty()) {
                throw new WeatherServiceException("Weather SVC returned empty data for " + city, HttpStatus.NO_CONTENT);
            }

            redisService.saveWithTTL(key, data, cacheTTL);
            log.info("Saved weather data in Redis for {} with TTL: {}s", city, cacheTTL);

            return new CacheResult(data, false);

        } catch (HttpStatusCodeException httpEx) {
            HttpStatus status;
            try {
                status = HttpStatus.valueOf(httpEx.getStatusCode().value());
            } catch (IllegalArgumentException e) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            String msg = switch (status) {
                case NOT_FOUND -> "City not found: " + city;
                case TOO_MANY_REQUESTS -> "Weather SVC rate limit exceeded. Try again later.";
                case BAD_REQUEST -> "Invalid city name or request format.";
                case BAD_GATEWAY, SERVICE_UNAVAILABLE -> handleServiceUnavailable(city, key);
                default -> "Unexpected HTTP error (" + status.value() + ") from Weather SVC.";
            };

            throw new WeatherServiceException(msg, httpEx, status);

        } catch (RestClientException rcEx) {
            throw new WeatherServiceException("Error communicating with Weather SVC for " + city, rcEx, HttpStatus.BAD_GATEWAY);

        } catch (Exception ex) {
            throw new WeatherServiceException("Unexpected error fetching weather for " + city, ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String handleServiceUnavailable(String city, String key) {
        Object stale = redisService.get(key);
        if (stale != null) {
            log.warn("Weather SVC down. Returning stale cache for {}", city);
            return "Weather SVC temporarily unavailable. Returning last known data.";
        }
        return "Weather SVC temporarily unavailable. Please retry later.";
    }
}