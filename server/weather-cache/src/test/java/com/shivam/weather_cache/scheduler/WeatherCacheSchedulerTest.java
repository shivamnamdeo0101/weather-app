package com.shivam.weather_cache.scheduler;

import com.shivam.weather_cache.service.GenericRedisServiceImpl;
import com.shivam.weather_cache.strategy.CacheRefreshStrategy;
import com.shivam.weather_cache.strategy.CacheRefreshStrategyFactory;
import com.shivam.weather_cache.strategy.HotCityRefreshStrategy;
import com.shivam.weather_cache.strategy.MediumCityRefreshStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WeatherCacheSchedulerTest {

    private GenericRedisServiceImpl redisService;
    private CacheRefreshStrategyFactory strategyFactory;
    private WeatherCacheScheduler scheduler;

    @BeforeEach
    void setup() {
        redisService = mock(GenericRedisServiceImpl.class);
        strategyFactory = mock(CacheRefreshStrategyFactory.class);

        scheduler = new WeatherCacheScheduler(redisService, strategyFactory);
    }

    @Test
    void testRefreshCache_noCities_logsAndReturns() {
        when(redisService.getAllKeys("weather:*:data")).thenReturn(Set.of());

        scheduler.refreshCache();

        verify(redisService, times(1)).getAllKeys("weather:*:data");
        verifyNoMoreInteractions(strategyFactory);
    }

    @Test
    void testRefreshCache_hotAndMediumCities_refreshCalled() {
        // Arrange: mock Redis keys
        Set<String> keys = Set.of("weather:city1:data", "weather:city2:data");
        when(redisService.getAllKeys("weather:*:data")).thenReturn(keys);

        // Arrange: mock strategies
        CacheRefreshStrategy hotStrategy = mock(HotCityRefreshStrategy.class);
        CacheRefreshStrategy mediumStrategy = mock(MediumCityRefreshStrategy.class);

        when(strategyFactory.getStrategy("weather:city1")).thenReturn(hotStrategy);
        when(strategyFactory.getStrategy("weather:city2")).thenReturn(mediumStrategy);

        when(hotStrategy.refreshIfRequired("weather:city1")).thenReturn(true);
        when(mediumStrategy.refreshIfRequired("weather:city2")).thenReturn(false);

        // Act
        scheduler.refreshCache();

        // Assert: verify strategy methods called
        verify(hotStrategy, times(1)).refreshIfRequired("weather:city1");
        verify(mediumStrategy, times(1)).refreshIfRequired("weather:city2");

        // verify factory called for each city
        verify(strategyFactory, times(1)).getStrategy("weather:city1");
        verify(strategyFactory, times(1)).getStrategy("weather:city2");
    }

    @Test
    void testRefreshCache_strategyThrowsException_handledGracefully() {
        Set<String> keys = Set.of("weather:city1:data");
        when(redisService.getAllKeys("weather:*:data")).thenReturn(keys);

        CacheRefreshStrategy hotStrategy = mock(HotCityRefreshStrategy.class);
        when(strategyFactory.getStrategy("weather:city1")).thenReturn(hotStrategy);
        when(hotStrategy.refreshIfRequired(anyString())).thenThrow(new RuntimeException("Some error"));

        scheduler.refreshCache();

        verify(hotStrategy, times(1)).refreshIfRequired("weather:city1");
        verify(strategyFactory, times(1)).getStrategy("weather:city1");
    }
}
