package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MediumCityRefreshStrategyTest {

    private GenericRedisService redisService;
    private WeatherSvcClient weatherSvcClient;
    private MediumCityRefreshStrategy mediumStrategy;

    @BeforeEach
    void setup() throws Exception {
        redisService = mock(GenericRedisService.class);
        weatherSvcClient = mock(WeatherSvcClient.class);

        mediumStrategy = new MediumCityRefreshStrategy(redisService, weatherSvcClient);

        // Use reflection to set private fields
        setPrivateField(mediumStrategy, "mediumHitThreshold", 10L);
        setPrivateField(mediumStrategy, "mediumRefreshInterval", 1000L); // 1 second
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testRefreshIfRequired_belowThreshold_returnsFalse() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 5L);
        meta.put("lastRefresh", Instant.now().toEpochMilli() - 5000);

        when(redisService.getMeta("weather:testCity")).thenReturn(meta);

        boolean result = mediumStrategy.refreshIfRequired("weather:testCity");

        assertThat(result).isFalse();
        verifyNoInteractions(weatherSvcClient);
    }

    @Test
    void testRefreshIfRequired_intervalNotPassed_returnsFalse() {
        long now = Instant.now().toEpochMilli();
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 20L);
        meta.put("lastRefresh", now); // last refresh just now

        when(redisService.getMeta("weather:testCity")).thenReturn(meta);

        boolean result = mediumStrategy.refreshIfRequired("weather:testCity");

        assertThat(result).isFalse();
        verifyNoInteractions(weatherSvcClient);
    }

    @Test
    void testRefreshIfRequired_refreshHappens_callsWeatherSvcAndRedis() {
        long now = Instant.now().toEpochMilli();
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 20L);
        meta.put("lastRefresh", now - 2000); // interval passed

        when(redisService.getMeta("weather:testCity")).thenReturn(meta);
        when(weatherSvcClient.fetchWeatherData("testCity")).thenReturn(Map.of("temp", 25));

        boolean result = mediumStrategy.refreshIfRequired("weather:testCity");

        assertThat(result).isTrue();

        verify(weatherSvcClient, times(1)).fetchWeatherData("testCity");

        ArgumentCaptor<Long> hitsCaptor = ArgumentCaptor.forClass(Long.class);
        verify(redisService).saveWithMeta(eq("weather:testCity"), any(), eq(true), hitsCaptor.capture());

        // After refresh, hits should be reset to 1
        assertThat(hitsCaptor.getValue()).isEqualTo(1L);
    }
}
