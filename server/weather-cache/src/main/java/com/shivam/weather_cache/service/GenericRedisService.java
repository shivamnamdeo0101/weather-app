package com.shivam.weather_cache.service;

import java.util.Map;
import java.util.Set;

public interface GenericRedisService {

    void saveWithMeta(String key, Object value, boolean refresh);

    Object getAndUpdateMeta(String key);

    Map<Object, Object> getMeta(String key);

    Set<String> getAllKeys(String pattern);

    void deleteKey(String key);
}
