package com.shivam.weather_cache.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class LowCityRefreshStrategyTest {

    private final LowCityRefreshStrategy lowStrategy = new LowCityRefreshStrategy();

    @Test
    void testRefreshIfRequired_AlwaysFalse() {
        // LOW strategy should never refresh
        boolean result = lowStrategy.refreshIfRequired("weather:somecity");
        assertFalse(result, "LowCityRefreshStrategy should always return false");
    }
}
