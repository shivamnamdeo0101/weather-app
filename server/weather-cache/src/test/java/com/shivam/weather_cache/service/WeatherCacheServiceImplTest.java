package com.shivam.weather_cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import com.shivam.weather_cache.strategy.CacheRefreshStrategy;
import com.shivam.weather_cache.strategy.CacheRefreshStrategyFactory;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class WeatherCacheServiceImplTest {

    @Mock private GenericRedisService redisService;
    @Mock private ObjectMapper objectMapper;
    @Mock private WeatherSvcClient weatherSvcClient;
    @Mock private CacheRefreshStrategyFactory strategyFactory;
    @Mock private CacheRefreshStrategy strategy;

    private WeatherCacheServiceImpl weatherCacheService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        weatherCacheService = new WeatherCacheServiceImpl(
                redisService,
                objectMapper,
                weatherSvcClient,
                strategyFactory
        );
    }

    @Test
    void testGetWeather_CacheHit() throws Exception {
        String city = "London";
        String key = "weather:" + city.toLowerCase();

        Map<String, Object> cachedMap = new HashMap<>();
        cachedMap.put("temp", 25);

        when(redisService.getAndUpdateMeta(key)).thenReturn(cachedMap);
        // Fix here
        when(objectMapper.convertValue(any(), ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                .thenReturn(cachedMap);
        when(strategyFactory.getStrategy(key)).thenReturn(strategy);
        when(strategy.refreshIfRequired(key)).thenReturn(true);

        CacheResult result = weatherCacheService.getWeather(city);

        assertThat(result.isCacheHit()).isTrue();
        assertThat(result.getData()).isEqualTo(cachedMap);

        verify(redisService).getAndUpdateMeta(key);
        verify(strategy).refreshIfRequired(key);
    }


    @Test
    void testGetWeather_CacheMiss_SvcReturnsData() throws Exception {
        String city = "Paris";
        String key = "weather:" + city.toLowerCase();

        Map<String, Object> svcData = new HashMap<>();
        svcData.put("temp", 30);

        when(redisService.getAndUpdateMeta(key)).thenReturn(null);
        when(weatherSvcClient.fetchWeatherData(city)).thenReturn(svcData);

        CacheResult result = weatherCacheService.getWeather(city);

        assertThat(result.isCacheHit()).isFalse();
        assertThat(result.getData()).isEqualTo(svcData);

        verify(weatherSvcClient).fetchWeatherData(city);
        verify(redisService).saveWithMeta(key, svcData, false, 1L);
    }

    @Test
    void testGetWeather_CacheMiss_SvcReturnsNull_ThrowsException() {
        String city = "InvalidCity";
        String key = "weather:" + city.toLowerCase();

        when(redisService.getAndUpdateMeta(key)).thenReturn(null);
        when(weatherSvcClient.fetchWeatherData(city)).thenReturn(Collections.emptyMap());

        WeatherServiceException ex = assertThrows(
                WeatherServiceException.class,
                () -> weatherCacheService.getWeather(city)
        );

        assertThat(ex.getMessage()).contains("City not found");
        verify(weatherSvcClient).fetchWeatherData(city);
    }

    @Test
    void testGetWeather_HttpStatusException() {
        String city = "Tokyo";
        String key = "weather:" + city.toLowerCase();

        HttpStatusCodeException httpEx = mock(HttpStatusCodeException.class);
        when(httpEx.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(httpEx.getResponseBodyAsString()).thenReturn("City not found");

        when(redisService.getAndUpdateMeta(key)).thenReturn(null);
        when(weatherSvcClient.fetchWeatherData(city)).thenThrow(httpEx);

        WeatherServiceException ex = assertThrows(
                WeatherServiceException.class,
                () -> weatherCacheService.getWeather(city)
        );

        assertThat(ex.getMessage()).contains("City not found");
    }

    @Test
    void testGetWeather_InvalidCity_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> weatherCacheService.getWeather(""));
        assertThrows(IllegalArgumentException.class, () -> weatherCacheService.getWeather("   "));
        assertThrows(IllegalArgumentException.class, () -> weatherCacheService.getWeather(null));
    }

    @Test
    void testGetWeather_RedisReadFails_SvcCalled() throws Exception {
        String city = "Berlin";
        String key = "weather:" + city.toLowerCase();

        when(redisService.getAndUpdateMeta(key)).thenThrow(new RuntimeException("Redis down"));

        Map<String, Object> svcData = Map.of("temp", 20);
        when(weatherSvcClient.fetchWeatherData(city)).thenReturn(svcData);

        CacheResult result = weatherCacheService.getWeather(city);

        assertThat(result.isCacheHit()).isFalse();
        assertThat(result.getData()).isEqualTo(svcData);
        verify(weatherSvcClient).fetchWeatherData(city);
    }
}
