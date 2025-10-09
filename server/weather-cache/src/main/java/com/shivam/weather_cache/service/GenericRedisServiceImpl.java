package com.shivam.weather_cache.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class GenericRedisServiceImpl implements GenericRedisService {

    @Value("${spring.redis.ttl}")
    private long cacheTTL;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public GenericRedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveWithMeta(String key, Object value, boolean refresh) {
        log.info("Saving weather data in redis for {} with TTL={}s Refresh :{}", key,cacheTTL,refresh);
        try {
            redisTemplate.execute(new SessionCallback<Object>() {
                @Override
                public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
                    HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

                    valueOps.set(key + ":data", value);
                    hashOps.put(key + ":meta", "hits", 1);
                    hashOps.put(key + ":meta", "lastAccess", Instant.now().toEpochMilli());
                    if(refresh){
                        hashOps.put(key + ":meta", "lastRefresh", Instant.now().toEpochMilli());
                    }

                    redisTemplate.expire(key + ":data", java.time.Duration.ofSeconds(cacheTTL));
                    redisTemplate.expire(key + ":meta", java.time.Duration.ofSeconds(cacheTTL));
                    return operations.exec();
                }
            });
        } catch (Exception ex) {
            log.error("Error in saveWithMeta for key '{}'", key, ex);
        }
    }

    @Override
    public Object getAndUpdateMeta(String key) {
        log.info("Get and update the meta data for {}",key);
        try {
            ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
            Object value = valueOps.get(key + ":data");

            if (value != null) {
                log.info("Cache HIT for city: {}", key);
                redisTemplate.execute(new SessionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
                        operations.multi();
                        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
                        hashOps.increment(key + ":meta", "hits", 1);
                        hashOps.put(key + ":meta", "lastAccess", Instant.now().toEpochMilli());
                        return operations.exec();
                    }
                });
            }
            return value;
        } catch (Exception ex) {
            log.error("Error in getAndUpdateMeta for key '{}'", key, ex);
            return null;
        }
    }


    @Override
    public Map<Object, Object> getMeta(String key) {
//        log.info("Fetching the meta data for {}",key);
        try {
            HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
            Map<Object, Object> metaData = hashOps.entries(key + ":meta");
            return metaData.isEmpty() ? Collections.emptyMap() : metaData;
        } catch (Exception ex) {
            log.error("Error in getMeta for key '{}'", key, ex);
            return Collections.emptyMap();
        }
    }

    @Override
    public Set<String> getAllKeys(String pattern) {
        log.info("Fetching all the keys from redis");
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception ex) {
            log.error("Error getting keys for pattern '{}'", pattern, ex);
            return Collections.emptySet();
        }
    }


    @Override
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key + ":data");
            redisTemplate.delete(key + ":meta");

        } catch (Exception ex) {
            log.error("Error deleting key '{}'", key, ex);
        }
    }
}
