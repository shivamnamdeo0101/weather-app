package com.shivam.weather_cache.scheduler;

import com.shivam.weather_cache.service.GenericRedisServiceImpl;
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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherCacheScheduler {

    private final GenericRedisServiceImpl redisService;
    private final WeatherSvcClient weatherSvcClient;

    @Value("${schedular.hot_hit_threshold}")
    private long HOT_HIT_THRESHOLD;

    @Value("${schedular.medium_hit_threshold}")
    private long MEDIUM_HIT_THRESHOLD;

    @Value("${schedular.hot_refresh_interval}")
    private long HOT_REFRESH_INTERVAL;

    @Value("${schedular.medium_refresh_interval}")
    private long MEDIUM_REFRESH_INTERVAL;

    @Value("${schedular.low_active_refresh_interval}")
    private long LOW_ACTIVE_REFRESH_INTERVAL;

    @Value("${schedular.max_age}")
    private long MAX_AGE;

    private final Random random = new Random();

    /**
     * Scheduler runs every 5 min:
     * - Refreshes HOT/MEDIUM cities asynchronously via virtual threads.
     * - Removes inactive cities.
     * - Staggered 0-500ms delay per city for SVC call will be in limit.
     * - Logs summary **after all threads complete**.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000L)
    public void refreshCache() {
        long now = Instant.now().toEpochMilli();
        log.info("Scheduler triggered at {}", DateTimeUtils.formatEpochMilli(now));

        try {
            Set<String> keys = redisService.getAllKeys("weather:*:data");
            if (keys == null || keys.isEmpty()) {
                log.info("No cached cities to process.");
                return;
            }

            Set<String> hotRefreshed = ConcurrentHashMap.newKeySet();
            Set<String> mediumRefreshed = ConcurrentHashMap.newKeySet();

            // Track virtual threads for join
            Thread[] threads = new Thread[keys.size()];
            int i = 0;

            for (String dataKey : keys) {
                final String cityDataKey = dataKey;
                threads[i++] = Thread.startVirtualThread(() -> {
                    try {
                        Thread.sleep(random.nextInt(500)); // stagger

                        String cityKey = cityDataKey.replace(":data", "");
                        Map<Object, Object> meta = safeMeta(redisService.getMeta(cityKey));

                        long hits = parseLong(meta.get("hits"));
                        long lastAccess = parseLong(meta.get("lastAccess"));
                        long lastRefresh = parseLong(meta.get("lastRefresh"));
                        long age = now - lastAccess;

                        if (hits >= HOT_HIT_THRESHOLD) {
                            log.info("HOT_ACTIVE_REFRESH");
                            handleRefresh(cityKey, meta, now, lastRefresh, HOT_REFRESH_INTERVAL, "🔥 HOT");
                            hotRefreshed.add(cityKey);
                        } else if (hits >= MEDIUM_HIT_THRESHOLD) {
                            log.info("MEDIUM_ACTIVE_REFRESH");
                            handleRefresh(cityKey, meta, now, lastRefresh, MEDIUM_REFRESH_INTERVAL, "🌤 MEDIUM");
                            mediumRefreshed.add(cityKey);
                        } else {
                            log.info("LOW_ACTIVE_REMOVAL");
                            log.info("LOW_ACTIVE city {} age is {} removed with TTL naturally : ", cityKey, (age / 1000 * 60));
                            log.info("LOW_ACTIVE cities expire naturally via Redis TTL of 1 hours, no scheduler refresh needed.");
                            //NOT REQ - handleRefresh(cityKey, meta, now, lastRefresh, LOW_ACTIVE_REFRESH_INTERVAL, "☁️ LOW_ACTIVE");
                            //NOT REQ - handleRemoval(cityKey, age);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Virtual thread interrupted for key: {}", cityDataKey);
                    } catch (Exception ex) {
                        log.error("Error processing city key: {}", cityDataKey, ex);
                    }
                });
            }

            // Wait for all virtual threads to complete before summary
            for (Thread t : threads) {
                t.join();
            }

            // Summary logging
            if (!hotRefreshed.isEmpty()) {
                log.info("Refreshed HOT cities: {}", hotRefreshed);
            }
            if (!mediumRefreshed.isEmpty()) {
                log.info("Refreshed MEDIUM cities: {}", mediumRefreshed);
            }
            if (hotRefreshed.isEmpty() && mediumRefreshed.isEmpty()) {
                log.info("No cities found to refresh");
            }

            log.info("Scheduler completed all virtual-thread refreshes");

        } catch (Exception ex) {
            log.error("Error in WeatherCacheScheduler", ex);
        }
    }

    private void handleRefresh(String cityKey, Map<Object, Object> meta, long now,
                               long lastRefresh, long interval, String level) {
        if (now - lastRefresh >= interval) {
            log.info("{} Refreshing weather data for '{}' , Meta Data: {}", level, cityKey, meta);
            String city = cityKey.split(":")[1];
            Object result = weatherSvcClient.fetchWeatherData(city);
            redisService.saveWithMeta(cityKey, result, true);
            log.info("Weather data refreshed for {} : {}", city, result);
        }
    }

    private void handleRemoval(String cityKey, long age) {
        log.info("Removing city '{}' from cache (inactive for {} min)", cityKey, age / 60000);
        redisService.deleteKey(cityKey);
    }

    private Map<Object, Object> safeMeta(Map<Object, Object> meta) {
        return meta != null ? meta : Collections.emptyMap();
    }

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
