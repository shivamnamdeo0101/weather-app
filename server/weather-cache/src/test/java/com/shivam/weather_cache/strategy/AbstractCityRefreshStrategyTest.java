package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractCityRefreshStrategyTest {

    @Mock
    private GenericRedisService redisService;

    @Mock
    private WeatherSvcClient weatherSvcClient;

    private TestCityRefreshStrategy strategy;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        strategy = new TestCityRefreshStrategy(redisService, weatherSvcClient);
        // Inject private field hotHitThreshold using reflection if needed
        setHotHitThreshold(strategy, 10);
    }

    private void setHotHitThreshold(AbstractCityRefreshStrategy strategy, long value) {
        try {
            var field = AbstractCityRefreshStrategy.class.getDeclaredField("hotHitThreshold");
            field.setAccessible(true);
            field.set(strategy, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRefreshIfRequired_belowThreshold_returnsFalse() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 1);
        meta.put("lastRefresh", System.currentTimeMillis() - 10000);
        when(redisService.getMeta("city:TestCity")).thenReturn(meta);

        boolean result = strategy.refreshIfRequired("city:TestCity");
        assertFalse(result);
        verify(weatherSvcClient, never()).fetchWeatherData(anyString());
        verify(redisService, never()).saveWithMeta(anyString(), any(), anyBoolean(), anyLong());
    }

    @Test
    void testRefreshIfRequired_intervalNotPassed_returnsFalse() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 20);
        meta.put("lastRefresh", System.currentTimeMillis());
        when(redisService.getMeta("city:TestCity")).thenReturn(meta);

        boolean result = strategy.refreshIfRequired("city:TestCity");
        assertFalse(result);
        verify(weatherSvcClient, never()).fetchWeatherData(anyString());
        verify(redisService, never()).saveWithMeta(anyString(), any(), anyBoolean(), anyLong());
    }

    @Test
    void testRefreshIfRequired_refreshesSuccessfully() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 20);
        meta.put("lastRefresh", System.currentTimeMillis() - 100000);
        when(redisService.getMeta("city:TestCity")).thenReturn(meta);
        when(weatherSvcClient.fetchWeatherData("TestCity")).thenReturn(Collections.singletonMap("temp", 25));

        boolean result = strategy.refreshIfRequired("city:TestCity");
        assertTrue(result);

        verify(weatherSvcClient).fetchWeatherData("TestCity");
        verify(redisService).saveWithMeta(eq("city:TestCity"), any(), eq(true), anyLong());
    }

    // Concrete subclass for testing
    static class TestCityRefreshStrategy extends AbstractCityRefreshStrategy {

        protected TestCityRefreshStrategy(GenericRedisService redisService, WeatherSvcClient weatherSvcClient) {
            super(redisService, weatherSvcClient);
        }

        @Override
        protected long getHitThreshold() {
            return 10;
        }

        @Override
        protected long getRefreshInterval() {
            return 5000; // 5 seconds for test
        }

        @Override
        protected String getLevelName() {
            return "TEST";
        }
    }
}
