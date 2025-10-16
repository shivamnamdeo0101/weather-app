package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import com.shivam.weather_cache.strategy.CacheRefreshStrategy;
import com.shivam.weather_cache.strategy.CacheRefreshStrategyFactory;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;
@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherCacheServiceImpl implements WeatherCacheService {

    private final GenericRedisService redisService;
    private final ObjectMapper objectMapper;
    private final WeatherSvcClient weatherSvcClient;
    private final CacheRefreshStrategyFactory strategyFactory;

    @Override
    public CacheResult getWeather(String city) {
        if (city == null || city.isBlank()) {
            log.warn("Received empty city parameter");
            throw new IllegalArgumentException("City parameter cannot be empty");
        }

        String key = "weather:" + city.toLowerCase();
        log.info("Fetching weather for city: {}", city);

        // --- Try cache first ---
        try {
            Object cached = redisService.getAndUpdateMeta(key);

            if (cached != null) {
                log.info("Cache HIT for city: {}", city);

                Map<String, Object> cachedMap = objectMapper.convertValue(cached, new TypeReference<>() {});

                // Optional refresh if strategy exists
                CacheRefreshStrategy strategy = strategyFactory.getStrategy(key);
                if (strategy != null && strategy.refreshIfRequired(key)) {
                    log.info("City {} refreshed on-demand via strategy {}", city, strategy.getClass().getSimpleName());
                }

                return new CacheResult(cachedMap, true);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for key '{}': {}", key, ex.toString());
        }

        // --- Cache MISS → call Weather SVC ---
        log.info("Cache MISS for city: {}. Calling Weather SVC...", city);
        try {
            Map<String, Object> data = weatherSvcClient.fetchWeatherData(city);

            if (data == null || data.isEmpty()) {
                log.warn("Weather SVC returned no data for city: {}", city);
                throw new WeatherServiceException("City not found: " + city, HttpStatus.NOT_FOUND);
            }

            // Save in Redis (best-effort)
            try {
                redisService.saveWithMeta(key, data, false, 1L);
            } catch (Exception ex) {
                log.warn("Redis save failed for key '{}': {}", key, ex.toString());
            }

            return new CacheResult(data, false);

        } catch (HttpStatusCodeException httpEx) {
            // Extract HTTP status from SVC
            HttpStatus status = HttpStatus.resolve(httpEx.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

            String msg = switch (status) {
                case NOT_FOUND -> "City not found: " + city;
                case TOO_MANY_REQUESTS -> "Weather SVC rate limit exceeded. Try again later.";
                case BAD_REQUEST -> "Invalid city name or request format.";
                case BAD_GATEWAY, SERVICE_UNAVAILABLE -> "Weather SVC temporarily unavailable. Please retry later.";
                default -> "Unexpected HTTP error (" + status.value() + ") from Weather SVC.";
            };

            log.error("Weather SVC HTTP exception for city '{}': {} - {}", city, status, httpEx.getResponseBodyAsString());
            throw new WeatherServiceException(msg, httpEx, status);

        } catch (WeatherServiceException wse) {
            log.warn("WeatherServiceException for city '{}': {}", city, wse.getMessage());
            throw wse;

        } catch (Exception ex) {
            log.error("Unexpected error fetching weather for city '{}': {}", city, ex.toString(), ex);
            throw new WeatherServiceException(
                    "Unexpected error fetching weather for " + city,
                    ex,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
