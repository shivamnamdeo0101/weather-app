package com.shivam.weather_cache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheManagerService {

    private final GenericRedisService redisService;
    private static final String WEATHER_KEY_PREFIX = "weather:";

    @Value("${spring.redis.ttl}")
    private long cacheTTL;

    public Set<String> getAllCityKeys() {
        return redisService.getAllKeys(WEATHER_KEY_PREFIX + "*:data");
    }

    public boolean deleteCityCache(String cityKey) {
        try {
            redisService.deleteKey(cityKey);
            log.info("✅ Deleted cache for '{}'", cityKey);
            return true;
        } catch (Exception ex) {
            log.error("❌ Failed to delete cache for '{}'", cityKey, ex);
            return false;
        }
    }

    public Map<Object, Object> getCityMetadata(String cityKey) {
        return redisService.getMeta(cityKey);
    }

    public Object getCityData(String cityKey) {
        return redisService.getData(cityKey);
    }

    public boolean refreshCityDataAndMeta(String cityKey,Object value) {
        try {
            redisService.saveWithMeta(cityKey,value,cacheTTL);
            return true;
        } catch (Exception ex) {
            log.error("❌ Failed to update lastRefresh for '{}'", cityKey, ex);
            return false;
        }
    }
}
