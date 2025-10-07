package com.shivam.weather_cache.service;

/**
 * GenericRedisService defines basic Redis operations for storing
 * and retrieving key-value data with optional TTL (time-to-live).
 *
 * <p>This interface allows for reusability and easy mocking in unit tests.</p>
 */
public interface GenericRedisService {

    /**
     * Saves a value in Redis with a specified TTL.
     *
     * @param key     the Redis key
     * @param value   the value to store
     * @param seconds the time-to-live in seconds
     */
    void saveWithTTL(String key, Object value, long seconds);

    /**
     * Retrieves a value from Redis by key.
     *
     * @param key the Redis key
     * @return the stored object, or null if not found
     */
    Object get(String key);
}
