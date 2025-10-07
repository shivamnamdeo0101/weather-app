package com.shivam.weather_cache.service;

import java.util.Map;
import java.util.Set;

public interface GenericRedisService {

    void saveWithMeta(String key, Object value, long ttlSeconds);

    Object getAndUpdateMeta(String key);

    Object getData(String key);

    Map<Object, Object> getMeta(String key);

    Set<String> getAllKeys(String pattern);

    void updateLastRefresh(String key, long timestamp);

    void deleteKey(String key);
}
