package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.Map;

/**
 * Service for fetching weather data with caching in Redis.
 * Handles:
 *  - Cache HIT / MISS logging
 *  - Redis access failures
 *  - RestTemplate timeouts / network errors
 *  - HTTP status code mapping
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherCacheServiceImpl implements WeatherCacheService {

    private final GenericRedisService redisService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${spring.redis.ttl}")
    private long cacheTTL;

    @Value("${weather.svc.url}")
    private String svcUrl;

    /**
     * Fetch weather data for a city.
     * @param city name of the city
     * @return CacheResult containing data and cache hit/miss info
     */
    @Override
    public CacheResult getWeather(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City parameter cannot be empty");
        }

        String key = "weather:" + city.toLowerCase();
        log.info("Fetching weather for city: {}", city);

        // 1️⃣ Try to fetch from Redis cache
        try {
            Object cached = redisService.getAndUpdateMeta(key);
            if (cached != null) {
                Map<String, Object> cachedMap = objectMapper.convertValue(cached, new TypeReference<>() {});
                log.info("Cache HIT for city: {}", city);
                return new CacheResult(cachedMap, true);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for key {}: {}", key, ex.getMessage());
            // continue to fetch from Weather API
        }

        log.info("Cache MISS for city: {}. Calling Weather SVC: {}", city, svcUrl);

        // 2️⃣ Call Weather SVC
        try {
            String url = svcUrl + "?city=" + city;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> data = response.getBody();
            if (data == null || data.isEmpty()) {
                throw new WeatherServiceException(
                        "Weather SVC returned empty data for " + city,
                        HttpStatus.NO_CONTENT
                );
            }

            // 3️⃣ Save in Redis
            try {
                redisService.saveWithMeta(key, data, cacheTTL);
                log.info("Saved weather data in Redis for {} with TTL: {}s", city, cacheTTL);
            } catch (Exception ex) {
                log.warn("Redis save failed for key {}: {}", key, ex.getMessage());
            }

            return new CacheResult(data, false);

        } catch (HttpStatusCodeException httpEx) {
            // Map HTTP errors to meaningful messages
            HttpStatus status = HttpStatus.resolve(httpEx.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

            String msg = switch (status) {
                case NOT_FOUND -> "City not found: " + city;
                case TOO_MANY_REQUESTS -> "Weather SVC rate limit exceeded. Try again later.";
                case BAD_REQUEST -> "Invalid city name or request format.";
                case BAD_GATEWAY, SERVICE_UNAVAILABLE -> handleServiceUnavailable(city, key);
                default -> "Unexpected HTTP error (" + status.value() + ") from Weather SVC.";
            };

            throw new WeatherServiceException(msg, httpEx, status);

        } catch (ResourceAccessException raEx) {
            // Handles timeouts and network errors
            log.error("Weather SVC timeout/network error for {}: {}", city, raEx.getMessage());
            throw new WeatherServiceException(
                    "Weather SVC not reachable for " + city,
                    raEx,
                    HttpStatus.GATEWAY_TIMEOUT
            );

        } catch (RestClientException rcEx) {
            // Generic RestTemplate exception
            throw new WeatherServiceException(
                    "Error communicating with Weather SVC for " + city,
                    rcEx,
                    HttpStatus.BAD_GATEWAY
            );

        } catch (Exception ex) {
            // Fallback for unexpected errors
            throw new WeatherServiceException(
                    "Unexpected error fetching weather for " + city,
                    ex,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Handles Weather SVC downtime. Returns stale cache if available.
     */
    private String handleServiceUnavailable(String city, String key) {
        try {
            Object stale = redisService.getData(key);
            if (stale != null) {
                log.warn("Weather SVC down. Returning stale cache for {}", city);
                return "Weather SVC temporarily unavailable. Returning last known data.";
            }
        } catch (Exception ex) {
            log.warn("Failed to retrieve stale cache for {}: {}", city, ex.getMessage());
        }
        return "Weather SVC temporarily unavailable. Please retry later.";
    }
}
