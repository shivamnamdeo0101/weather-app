package com.shivam.weather_cache.service;

import java.util.Map;
import java.util.Set;

public interface GenericRedisService {

    /**
     * Saves a value and its metadata (hits, lastAccess) with a TTL.
     * @param key The primary key.
     * @param value The object to cache.
     * @param ttlSeconds Time-to-live in seconds.
     */
    void saveWithMeta(String key, Object value, long ttlSeconds);

    /**
     * Retrieves a value and its metadata, and updates the metadata (increments hits, updates lastAccess).
     * @param key The primary key.
     * @return A map containing "value" and "meta" (which is another Map). Returns null if the key is not found.
     */
    Object getAndUpdateMeta(String key);


    /**
     * Retrieves only the cached value for a key without updating metadata.
     * @param key The primary key.
     * @return The cached value or null if not found.
     */
    Object getData(String key);

    /**
     * Retrieves only the metadata for a key without updating hits or lastAccess.
     * @param key The primary key.
     * @return A map of metadata fields (hits, lastAccess, lastRefresh, etc.) or null if not found.
     */
    Map<Object, Object> getMeta(String key);

    /**
     * Retrieves all Redis keys matching a pattern.
     * @param pattern The key pattern (e.g., "*:data").
     * @return A set of matching keys or null if none found.
     */
    Set<String> getAllKeys(String pattern);

    /**
     * Updates the last refresh timestamp for a key.
     * @param key The primary key.
     * @param timestamp Epoch milliseconds of the last refresh.
     */
    void updateLastRefresh(String key, long timestamp);

    /**
     * Deletes a key (both data and metadata) from Redis.
     * @param key The primary key.
     */
    void deleteKey(String key);
}
