package com.shivam.weather_svc.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowRateLimiterTest {

    private SlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiter = new SlidingWindowRateLimiter();

        // Set private fields via reflection for testing
        setField(rateLimiter, "maxRequestsPerMinute", 3);
        setField(rateLimiter, "windowSizeSeconds", 2L);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        assertTrue(rateLimiter.tryConsume(), "1st request should be allowed");
        assertTrue(rateLimiter.tryConsume(), "2nd request should be allowed");
        assertTrue(rateLimiter.tryConsume(), "3rd request should be allowed");
    }

    @Test
    void shouldBlockRequestsExceedingLimit() {
        // Consume max allowed
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiter.tryConsume());
        }
        // Next request should fail
        assertFalse(rateLimiter.tryConsume(), "4th request should be blocked due to rate limit");
    }

    @Test
    void shouldAllowRequestAfterWindowExpires() throws InterruptedException {
        // Consume max allowed
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiter.tryConsume());
        }
        // Wait for window to expire
        TimeUnit.SECONDS.sleep(3);
        assertTrue(rateLimiter.tryConsume(), "Request should be allowed after sliding window reset");
    }
}
