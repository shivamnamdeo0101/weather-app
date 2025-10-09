package com.shivam.weather_cache.scheduler;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.GenericRedisServiceImpl;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.utils.DateTimeUtils;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherCacheScheduler {

    private final GenericRedisServiceImpl redisService;
    private final WeatherCacheService cacheService;
    private final WeatherSvcClient weatherSvcClient;


    // Thresholds
    private static final long HOT_HIT_THRESHOLD = 50;
    private static final long MEDIUM_HIT_THRESHOLD = 20;

    // Refresh intervals (ms)
    private static final long HOT_REFRESH_INTERVAL = 5 * 60 * 1000L;       // 5 minutes
    private static final long MEDIUM_REFRESH_INTERVAL = 15 * 60 * 1000L;   // 15 minutes
    private static final long MAX_AGE = 60 * 60 * 1000L;                   // 1 hour

    /**
     * Scheduled task runs every 5 minutes.
     * - Refreshes "hot" or "medium" cities as needed with the latest weather data as per interval.
     * - Removes inactive cities.
     */
    //@Scheduled(fixedRate = 5 * 60 * 1000L)
    @Scheduled(fixedRate = 5 * 1000L)
    public void refreshCache() {
        long now = Instant.now().toEpochMilli();
        log.info("Scheduler triggered at {}", DateTimeUtils.formatEpochMilli(now));
        try {
            Set<String> keys = redisService.getAllKeys("weather:*:data");

            if (keys == null || keys.isEmpty()) {
                log.info("No cached cities to process.");
                return;
            }

            for (String dataKey : keys) {
                String cityKey = dataKey.replace(":data", "");
                Map<Object, Object> meta = safeMeta(redisService.getMeta(cityKey));

                long hits = parseLong(meta.get("hits"));
                long lastAccess = parseLong(meta.get("lastAccess"));
                long lastRefresh = parseLong(meta.get("lastRefresh"));
                long age = now - lastAccess;

                if (hits >= HOT_HIT_THRESHOLD) {
                    handleRefresh(cityKey, meta, now, lastRefresh, HOT_REFRESH_INTERVAL, "🔥 HOT");
                } else if (hits >= MEDIUM_HIT_THRESHOLD) {
                    handleRefresh(cityKey, meta, now, lastRefresh, MEDIUM_REFRESH_INTERVAL, "🌤 MEDIUM");
                } else if (age > MAX_AGE) {
                    handleRemoval(cityKey, age);
                }
            }
            log.info("Schedular completed");

        } catch (Exception ex) {
            log.error("Error in WeatherCacheScheduler", ex);
        }
    }

    /**
     * Handle refresh logic for hot/medium cities.
     */
    private void handleRefresh(String cityKey, Map<Object, Object> meta, long now,
                               long lastRefresh, long interval, String level) {
        if (now - lastRefresh >= interval) {
            log.info("{} Refreshing weather data for '{}' , Meta Data: {}", level, cityKey, meta);
            String city = cityKey.split(":")[1];
            Object result = weatherSvcClient.fetchWeatherData(city);
            redisService.saveWithMeta(cityKey,result,true);
            log.info("Weather data refreshed for {} : {}", city, result);
        }
    }

    /**
     * Handle stale data removal.
     */
    private void handleRemoval(String cityKey, long age) {
        log.info("Removing city '{}' from cache (inactive for {} min)", cityKey, age / 60000);
        redisService.deleteKey(cityKey);
    }


    /**
     * Safely handle null/empty meta maps.
     */
    private Map<Object, Object> safeMeta(Map<Object, Object> meta) {
        return meta != null ? meta : Collections.emptyMap();
    }

    /**
     * Safe parser for numeric metadata values.
     */
    private long parseLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
