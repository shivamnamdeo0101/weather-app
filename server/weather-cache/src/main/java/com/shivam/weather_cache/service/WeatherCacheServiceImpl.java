package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.Map;

/**
 * Service for fetching weather data with Redis caching.
 * Responsibilities:
 *  - Cache HIT / MISS
 *  - Redis Call / SVC Call
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherCacheServiceImpl implements WeatherCacheService {

    private final GenericRedisService redisService;
    private final ObjectMapper objectMapper;
    private final WeatherSvcClient weatherSvcClient;


    @Override
    public CacheResult getWeather(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City parameter cannot be empty");
        }

        String key = "weather:" + city.toLowerCase();
        log.info("Fetching weather for city: {}", city);

        //Try cache first
        try {
            Object cached = redisService.getAndUpdateMeta(key);
            if (cached != null) {
                Map<String, Object> cachedMap = objectMapper.convertValue(cached, new TypeReference<>() {});
                return new CacheResult(cachedMap, true);
            }
        } catch (Exception ex) {
            log.warn("Redis read failed for key {}: {}", key, ex.getMessage());
        }

        log.info("Cache MISS for city: {}. Calling Weather SVC...", city);

        //Call the Weather API via weatherSvcClient
        try {
            Map<String, Object> data = weatherSvcClient.fetchWeatherData(city);

            if (data == null || data.isEmpty()) {
                throw new WeatherServiceException(
                        "Weather SVC returned empty data for " + city,
                        HttpStatus.NO_CONTENT
                );
            }

            //Save Date In the redis
            try {
                redisService.saveWithMeta(key, data,false);
            } catch (Exception ex) {
                log.warn("Redis save failed for key {}: {}", key, ex.getMessage());
            }

            return new CacheResult(data, false);

        } catch (HttpStatusCodeException httpEx) {
            HttpStatus status = HttpStatus.resolve(httpEx.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

            String msg = switch (status) {
                case NOT_FOUND -> "City not found: " + city;
                case TOO_MANY_REQUESTS -> "Weather SVC rate limit exceeded. Try again later.";
                case BAD_REQUEST -> "Invalid city name or request format.";
                case BAD_GATEWAY, SERVICE_UNAVAILABLE -> "Weather SVC temporarily unavailable. Please retry later.";
                default -> "Unexpected HTTP error (" + status.value() + ") from Weather SVC.";
            };

            throw new WeatherServiceException(msg, httpEx, status);

        } catch (Exception ex) {
            throw new WeatherServiceException(
                    "Unexpected error fetching weather for " + city,
                    ex,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
