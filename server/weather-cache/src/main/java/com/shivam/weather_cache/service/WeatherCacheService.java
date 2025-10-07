package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class WeatherCacheService {

    @Autowired
    private GenericRedisService redisService; // Assumed service for Redis operations

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

        // 1️⃣ Check cache first (fresh data)
        Object cached = redisService.get(key);
        if (cached != null) {
            Map<String, Object> cachedMap = objectMapper.convertValue(cached, new TypeReference<>() {});
            log.info("Cache HIT (fresh) for city: {}", city);
            return new CacheResult(cachedMap, true);
        }

        // 2️⃣ Call Weather SVC
        try {
            String url = svcUrl + "?city=" + city;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> data = response.getBody();
            if (data == null || data.isEmpty()) {
                throw new WeatherServiceException("No data found for city: " + city, HttpStatus.NO_CONTENT);
            }

            // 3️⃣ Cache MISS: Save and return fresh data
            redisService.saveWithTTL(key, data, cacheTTL);
            log.info("Cache MISS. Saved weather data for {} with TTL {}s", city, cacheTTL);
            return new CacheResult(data, false);

        } catch (HttpServerErrorException serverEx) {
            // 5xx errors (e.g., 500, 502, 503): Try to serve stale cache
            log.error("Weather SVC 5xx error ({}): {}", serverEx.getStatusCode().value(), serverEx.getResponseBodyAsString());

            // 🆕 STALE CACHE LOGIC: Attempt to retrieve and serve stale data
            // Assuming get(key, true) retrieves the data even if its TTL has expired
            Object stale = redisService.get(key);
            if (stale != null) {
                Map<String, Object> staleMap = objectMapper.convertValue(stale, new TypeReference<>() {});
                log.warn("Weather SVC error, serving STALE cache for city: {}", city);
                return new CacheResult(staleMap, true);
            }
            // 🔚 END STALE CACHE LOGIC

            throw new WeatherServiceException(
                    "Weather service temporarily unavailable. Please try again later.",
                    serverEx,
                    HttpStatus.BAD_GATEWAY // Maps to 502
            );

        } catch (ResourceAccessException raEx) {
            // Network / connection refused: Try to serve stale cache
            log.error("Weather SVC unreachable: {}", raEx.getMessage());

            // 🆕 STALE CACHE LOGIC: Attempt to retrieve and serve stale data
            Object stale = redisService.get(key);
            if (stale != null) {
                Map<String, Object> staleMap = objectMapper.convertValue(stale, new TypeReference<>() {});
                log.warn("Weather SVC unreachable, serving STALE cache for city: {}", city);
                return new CacheResult(staleMap, true);
            }
            // 🔚 END STALE CACHE LOGIC

            throw new WeatherServiceException(
                    "Weather service temporarily unreachable. Please try again later.",
                    raEx,
                    HttpStatus.BAD_GATEWAY // Maps to 502
            );
        }
    }

}
