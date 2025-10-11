package com.shivam.weather_cache.scheduler;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.GenericRedisServiceImpl;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.utils.DateTimeUtils;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final WeatherSvcClient weatherSvcClient;

    // Thresholds
    @Value("${schedular.hot_hit_threshold}")
    private long HOT_HIT_THRESHOLD;

    @Value("${schedular.medium_hit_threshold}")
    private long MEDIUM_HIT_THRESHOLD;

    // Refresh intervals
    @Value("${schedular.hot_refresh_interval}")
    private long HOT_REFRESH_INTERVAL;

    @Value("${schedular.medium_refresh_interval}")
    private long MEDIUM_REFRESH_INTERVAL;

    @Value("${schedular.max_age}")
    private long MAX_AGE;

    /**
     * Scheduled task runs every 5 minutes.
     * - Refreshes "hot" or "medium" cities as needed with the latest weather data as per interval.
     * - Removes inactive cities.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000L) //5 min
    public void refreshCache() {
        long now = Instant.now().toEpochMilli();
        log.info("Scheduler triggered at {}", DateTimeUtils.formatEpochMilli(now));
        try {
            Set<String> keys = redisService.getAllKeys("weather:*:data");

            if (keys == null || keys.isEmpty()) {
                log.info("No cached cities to process.");
                return;
            }
            String refreshKeyFor = null;
            for (String dataKey : keys) {
                String cityKey = dataKey.replace(":data", "");
                Map<Object, Object> meta = safeMeta(redisService.getMeta(cityKey));

                long hits = parseLong(meta.get("hits"));
                long lastAccess = parseLong(meta.get("lastAccess"));
                long lastRefresh = parseLong(meta.get("lastRefresh"));
                long age = now - lastAccess;

                if (hits >= HOT_HIT_THRESHOLD) {
                    refreshKeyFor = "HOT";
                    handleRefresh(cityKey, meta, now, lastRefresh, HOT_REFRESH_INTERVAL, "🔥 HOT");
                } else if (hits >= MEDIUM_HIT_THRESHOLD) {
                    refreshKeyFor = "MEDIUM";
                    handleRefresh(cityKey, meta, now, lastRefresh, MEDIUM_REFRESH_INTERVAL, "🌤 MEDIUM");
                } else if (age > MAX_AGE) {
                    refreshKeyFor = "INACTIVE";
                    handleRemoval(cityKey, age);
                }
            }

            if(refreshKeyFor != null){
                log.info("Refreshed and updated "+ refreshKeyFor);
            }else{
                log.info("No cities found to refresh");
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
