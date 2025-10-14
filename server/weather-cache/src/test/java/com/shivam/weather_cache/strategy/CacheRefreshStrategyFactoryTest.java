package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CacheRefreshStrategyFactoryTest {

    private GenericRedisService redisService;
    private HotCityRefreshStrategy hotStrategy;
    private MediumCityRefreshStrategy mediumStrategy;
    private LowCityRefreshStrategy lowStrategy;
    private CacheRefreshStrategyFactory factory;

    @BeforeEach
    void setUp() {
        redisService = mock(GenericRedisService.class);
        hotStrategy = mock(HotCityRefreshStrategy.class);
        mediumStrategy = mock(MediumCityRefreshStrategy.class);
        lowStrategy = mock(LowCityRefreshStrategy.class);

        // stub getHitThreshold only for Hot & Medium, LOW doesn't need it
        when(hotStrategy.getHitThreshold()).thenReturn(100L);
        when(mediumStrategy.getHitThreshold()).thenReturn(50L);

        factory = new CacheRefreshStrategyFactory(hotStrategy, mediumStrategy, lowStrategy, redisService);
    }

    @Test
    void testGetStrategy_HotCity() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 120L);
        when(redisService.getMeta("weather:city1")).thenReturn(meta);

        CacheRefreshStrategy strategy = factory.getStrategy("weather:city1");
        assertEquals(hotStrategy, strategy);
    }

    @Test
    void testGetStrategy_MediumCity() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 60L);
        when(redisService.getMeta("weather:city2")).thenReturn(meta);

        CacheRefreshStrategy strategy = factory.getStrategy("weather:city2");
        assertEquals(mediumStrategy, strategy);
    }

    @Test
    void testGetStrategy_LowCity() {
        Map<Object, Object> meta = new HashMap<>();
        meta.put("hits", 10L);
        when(redisService.getMeta("weather:city3")).thenReturn(meta);

        CacheRefreshStrategy strategy = factory.getStrategy("weather:city3");
        assertEquals(lowStrategy, strategy);
    }

    @Test
    void testGetStrategy_MetaNull() {
        when(redisService.getMeta("weather:city4")).thenReturn(null);
        CacheRefreshStrategy strategy = factory.getStrategy("weather:city4");
        assertEquals(lowStrategy, strategy);
    }
}
