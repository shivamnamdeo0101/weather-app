package com.shivam.weather_cache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class GenericRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveWithTTL(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}

