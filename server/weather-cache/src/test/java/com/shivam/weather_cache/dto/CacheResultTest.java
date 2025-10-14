package com.shivam.weather_cache.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CacheResultTest {

    @Test
    void testCacheResultProperties() {
        Map<String, Object> data = Map.of("temp", 25, "humidity", 80);
        boolean cacheHit = true;

        CacheResult result = new CacheResult(data, cacheHit);

        // Check that data is correctly set
        assertThat(result.getData()).isEqualTo(data);

        // Check that cacheHit is correctly set
        assertThat(result.isFromCache()).isTrue();
    }

    @Test
    void testCacheMiss() {
        Map<String, Object> data = Map.of("temp", 30);
        boolean cacheHit = false;

        CacheResult result = new CacheResult(data, cacheHit);

        assertThat(result.getData()).containsEntry("temp", 30);
        assertThat(result.isFromCache()).isFalse();
    }
}
