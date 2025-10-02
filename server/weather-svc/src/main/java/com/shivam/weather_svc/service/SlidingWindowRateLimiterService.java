package com.shivam.weather_svc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Thread-safe sliding window rate limiter.
 * Supports high concurrency for single-instance Spring Boot applications.
 */
@Service
@Slf4j
public class SlidingWindowRateLimiterService {

    @Value("${rate_limiter_max_req_per_min}")
    private int maxRequestsPerMinute;
    @Value("${rate_limiter_max_window_size_in_sec}")
    private long windowSizeSeconds;

    // Thread-safe deque to store timestamps of requests
    private final Deque<Long> requestTimestamps = new ConcurrentLinkedDeque<>();

    /**
     * Attempt to consume a request slot.
     *
     * @return true if request is allowed, false if rate limit exceeded
     */

    public synchronized boolean tryConsume() {
        long now = Instant.now().toEpochMilli();

        while (!requestTimestamps.isEmpty() && now - requestTimestamps.peekFirst() >= windowSizeSeconds * 1000) {
            requestTimestamps.pollFirst();
        }

        if (requestTimestamps.size() < maxRequestsPerMinute) {
            requestTimestamps.addLast(now);
            return true;
        } else {
            return false;
        }
    }

}
