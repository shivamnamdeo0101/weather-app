package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.log;
import org.slf4j.logFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
        String key = "weather:" + city.toLowerCase();
        log.info("Fetching weather for city: {}", city);

        // Check cache
        Object cached = redisService.get(key);
        if (cached != null) {
            Map<String, Object> map = objectMapper.convertValue(cached, new TypeReference<Map<String,Object>>() {});
            log.info("Cache HIT for city: {}", city);
            return new CacheResult(map, true); // HIT
        }

        log.info("Cache MISS for city: {}. Calling Weather SVC at {}", city, svcUrl);

        try {
            String url = svcUrl + "?city=" + city;
            ResponseEntity<Map<String,Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String,Object>>() {}
            );

            Map<String,Object> freshData = response.getBody();
            if (freshData == null || freshData.isEmpty()) {
                throw new WeatherServiceException("Weather SVC returned empty data for city: " + city);
            }

            log.info("Received weather data from Weather SVC for city: {}", city);

            // Save in Redis with TTL
            redisService.saveWithTTL(key, freshData, cacheTTL);
            log.info("Saved weather data in Redis for city: {} with TTL: {} seconds", city, cacheTTL);

            return new CacheResult(freshData, false); // MISS

        } catch (HttpStatusCodeException httpEx) {
            log.error("HTTP error fetching weather data for {}. Status: {}, Response: {}",
                    city, httpEx.getStatusCode(), httpEx.getResponseBodyAsString(), httpEx);
            throw new WeatherServiceException("HTTP error fetching weather for city: " + city, httpEx);
        } catch (RestClientException rcEx) {
            log.error("RestClientException fetching weather data for city: {}", city, rcEx);
            throw new WeatherServiceException("Error calling Weather SVC for city: " + city, rcEx);
        } catch (Exception ex) {
            log.error("Unexpected exception fetching weather data for city: {}", city, ex);
            throw new WeatherServiceException("Unexpected error fetching weather data for city: " + city, ex);
        }
    }
}
