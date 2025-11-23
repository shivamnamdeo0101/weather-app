package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCityRefreshStrategy implements CacheRefreshStrategy {

    @Value("${schedular.hot_hit_threshold}")
    private long hotHitThreshold;

    protected final GenericRedisService redisService;
    protected final WeatherSvcClient weatherSvcClient;


    protected abstract long getHitThreshold();
    protected abstract long getRefreshInterval();
    protected abstract String getLevelName();

    @Override
    public boolean refreshIfRequired(String cityKey) {
        Map<Object, Object> meta = redisService.getMeta(cityKey);
        if (meta == null || meta.isEmpty()) return false;

        long hits = meta.get("hits") == null ? 0 : Long.parseLong(meta.get("hits").toString());
        long lastRefresh = meta.get("lastRefresh") == null ? 0 : Long.parseLong(meta.get("lastRefresh").toString());
        long now = Instant.now().toEpochMilli();

        if (hits < getHitThreshold()) return false; // below threshold
        if (now - lastRefresh < getRefreshInterval()) return false; // interval not passed

        log.info("{} Refreshing cityKey={} hits={} lastRefresh={}", getLevelName(), cityKey, hits, lastRefresh);
        String city = cityKey.split(":")[1];
        Object result = weatherSvcClient.fetchWeatherData(city);

        long updatedHits = hits >= hotHitThreshold ? 1 : hits + 1; // reset only if HOT threshold crossed
        redisService.saveWithMeta(cityKey, result, true, updatedHits);

        return true;
    }

}
