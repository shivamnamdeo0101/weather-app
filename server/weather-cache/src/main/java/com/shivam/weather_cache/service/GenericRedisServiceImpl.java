package com.shivam.weather_cache.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class GenericRedisServiceImpl implements GenericRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public GenericRedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Save data along with metadata (hits, lastAccess) atomically.
     * TTL applied to both data and meta.
     */
    public void saveWithMeta(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.execute(new SessionCallback<Object>() {
                @Override
                public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
                    operations.multi(); // start transaction

                    // Typed operations to avoid unchecked warnings
                    ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
                    HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

                    // Save the actual value
                    valueOps.set(key + ":data", value);

                    // Increment hits atomically
                    hashOps.increment(key + ":meta", "hits", 1);

                    // Update lastAccess timestamp
                    hashOps.put(key + ":meta", "lastAccess", Instant.now().toEpochMilli());

                    // Set TTL for both data and meta
                    redisTemplate.expire(key + ":data", java.time.Duration.ofSeconds(ttlSeconds));
                    redisTemplate.expire(key + ":meta", java.time.Duration.ofSeconds(ttlSeconds));

                    return operations.exec(); // execute all commands atomically
                }
            });
        } catch (Exception ex) {
            log.error("❌ Error in saveWithMeta for key '{}'", key, ex);
        }
    }

    /**
     * Get data along with metadata and update hits/lastAccess atomically. => For FE/USER Call
     * we need to increment meta data
     */
    public Map<String, Object> getWithMeta(String key) {
        Map<String, Object> result = new HashMap<>();

        try {
            redisTemplate.execute(new SessionCallback<Object>() {
                @Override
                public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
                    operations.multi(); // start transaction

                    // Typed operations
                    ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
                    HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

                    // Get data
                    Object value = valueOps.get(key + ":data");

                    if (value != null) {
                        // Increment hits
                        hashOps.increment(key + ":meta", "hits", 1);

                        // Update lastAccess
                        hashOps.put(key + ":meta", "lastAccess", Instant.now().toEpochMilli());
                    }

                    // Get metadata
                    Map<String, Object> meta = hashOps.entries(key + ":meta");

                    result.put("value", value);
                    result.put("meta", meta.isEmpty() ? null : meta);

                    return operations.exec(); // execute transaction
                }
            });
        } catch (Exception ex) {
            log.error("❌ Error in getWithMeta for key '{}'", key, ex);
            result.put("value", null);
            result.put("meta", null);
        }

        return result;
    }

    // Update last refresh timestamp atomically
    public void updateLastRefresh(String key, long timestamp) {
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            hashOps.put(key + ":meta", "lastRefresh", timestamp);
        } catch (Exception ex) {
            log.error("❌ Error updating lastRefresh for key '{}'", key, ex);
        }
    }

    // Get all keys matching a pattern
    public Set<String> getAllKeys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception ex) {
            log.error("❌ Error getting keys with pattern '{}'", pattern, ex);
            return null;
        }
    }

    /**
     * Get only the data for a key, without touching metadata.
     */
    public Object getData(String key) {
        try {
            ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
            return valueOps.get(key + ":data");
        } catch (Exception ex) {
            log.error("❌ Error in getData for key '{}'", key, ex);
            return null;
        }
    }

    /**
     * Get only the metadata for a key, without touching hits or lastAccess.
     */
    public Map<Object, Object> getMeta(String key) {
        try {
            HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
            Map<Object, Object> metaData = hashOps.entries(key + ":meta");

            if (metaData.isEmpty()) {
                log.warn("⚠️ No metadata found for key '{}'", key);
                return Collections.emptyMap();
            }
            return metaData;

        } catch (Exception ex) {
            log.error("❌ Error in getMeta for key '{}': {}", key, ex.getMessage(), ex);
            return Collections.emptyMap();
        }
    }




    // Delete a city key (data + meta)
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key + ":data");
            redisTemplate.delete(key + ":meta");
        } catch (Exception ex) {
            log.error("❌ Error deleting key '{}'", key, ex);
        }
    }
}
