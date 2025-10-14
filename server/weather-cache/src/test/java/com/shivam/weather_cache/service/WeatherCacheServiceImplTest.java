package com.shivam.weather_cache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.exception.WeatherServiceException;
import com.shivam.weather_cache.strategy.CacheRefreshStrategy;
import com.shivam.weather_cache.strategy.CacheRefreshStrategyFactory;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherCacheServiceImplTest {

    private GenericRedisService redisService;
    private ObjectMapper objectMapper;
    private WeatherSvcClient weatherSvcClient;
    private CacheRefreshStrategyFactory strategyFactory;
    private WeatherCacheServiceImpl service;

    @BeforeEach
    void setup() {
        redisService = mock(GenericRedisService.class);
        objectMapper = new ObjectMapper();
        weatherSvcClient = mock(WeatherSvcClient.class);
        strategyFactory = mock(CacheRefreshStrategyFactory.class);

        service = new WeatherCacheServiceImpl(redisService, objectMapper, weatherSvcClient, strategyFactory);
    }

    @Test
    void testGetWeather_cacheHit_strategyRefresh() throws Exception {
        String city = "London";
        String key = "weather:london";

        Map<String, Object> cachedData = new HashMap<>();
        cachedData.put("temp", 25);

        when(redisService.getAndUpdateMeta(key)).thenReturn(cachedData);

        CacheRefreshStrategy strategy = mock(CacheRefreshStrategy.class);
        when(strategyFactory.getStrategy(key)).thenReturn(strategy);
        when(strategy.refreshIfRequired(key)).thenReturn(true);

        CacheResult result = service.getWeather(city);

        assertTrue(result.isFromCache());
        assertEquals(25, result.getData().get("temp"));

        verify(redisService).getAndUpdateMeta(key);
        verify(strategy).refreshIfRequired(key);
    }

    @Test
    void testGetWeather_cacheMiss_fetchFromSvcAndSave() throws Exception {
        String city = "Paris";
        String key = "weather:paris";

        when(redisService.getAndUpdateMeta(key)).thenReturn(null);

        Map<String, Object> fetchedData = new HashMap<>();
        fetchedData.put("temp", 18);
        when(weatherSvcClient.fetchWeatherData(city)).thenReturn(fetchedData);

        CacheResult result = service.getWeather(city);

        assertFalse(result.isFromCache());
        assertEquals(18, result.getData().get("temp"));

        verify(weatherSvcClient).fetchWeatherData(city);
        verify(redisService).saveWithMeta(eq(key), eq(fetchedData), eq(false), anyLong());
    }

    @Test
    void testGetWeather_invalidCity_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.getWeather(" "));
        assertThrows(IllegalArgumentException.class, () -> service.getWeather(null));
    }

    @Test
    void testGetWeather_weatherSvcReturnsEmpty_throwsWeatherServiceException() throws Exception {
        String city = "Berlin";
        String key = "weather:berlin";

        when(redisService.getAndUpdateMeta(key)).thenReturn(null);
        when(weatherSvcClient.fetchWeatherData(city)).thenReturn(new HashMap<>());

        WeatherServiceException ex = assertThrows(WeatherServiceException.class, () -> service.getWeather(city));
        assertEquals(HttpStatus.NO_CONTENT, ex.getStatus());
        assertTrue(ex.getMessage().contains("Weather SVC returned empty data"));
    }

    @Test
    void testGetWeather_weatherSvcHttpError_throwsWeatherServiceException() throws Exception {
        String city = "Rome";
        String key = "weather:rome";

        when(redisService.getAndUpdateMeta(key)).thenReturn(null);

        HttpStatusCodeException httpEx = mock(HttpStatusCodeException.class);
        when(httpEx.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(weatherSvcClient.fetchWeatherData(city)).thenThrow(httpEx);

        WeatherServiceException ex = assertThrows(WeatherServiceException.class, () -> service.getWeather(city));
        assertEquals("Invalid city name or request format.", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
