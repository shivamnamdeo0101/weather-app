package com.shivam.weather_cache.scheduler;

import com.shivam.weather_cache.service.GenericRedisServiceImpl;
import com.shivam.weather_cache.strategy.CacheRefreshStrategy;
import com.shivam.weather_cache.strategy.CacheRefreshStrategyFactory;
import com.shivam.weather_cache.strategy.HotCityRefreshStrategy;
import com.shivam.weather_cache.strategy.MediumCityRefreshStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherCacheScheduler {

    private final GenericRedisServiceImpl redisService;
    private final CacheRefreshStrategyFactory strategyFactory;
    private final Random random = new Random();

    @Scheduled(fixedRate = 15 * 60 * 1000L)
    public void refreshCache() {
        log.info("Scheduler triggered");

        Set<String> keys = redisService.getAllKeys("weather:*:data");
        if (keys == null || keys.isEmpty()) {
            log.info("No cities found in Redis to refresh. Scheduler completed.");
            return;
        }

        Set<String> hotRefreshed = ConcurrentHashMap.newKeySet();
        Set<String> mediumRefreshed = ConcurrentHashMap.newKeySet();

        Thread[] threads = new Thread[keys.size()];
        int i = 0;

        for (String key : keys) {
            threads[i++] = Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(random.nextInt(500)); // small random delay to reduce spikes
                    String cityKey = key.replace(":data", "");

                    CacheRefreshStrategy strategy = strategyFactory.getStrategy(cityKey);
                    boolean refreshed = strategy.refreshIfRequired(cityKey);

                    if (refreshed) {
                        String level = strategy instanceof HotCityRefreshStrategy ? "HOT" :
                                strategy instanceof MediumCityRefreshStrategy ? "MEDIUM" : "LOW";
                        if ("HOT".equals(level)) hotRefreshed.add(cityKey);
                        if ("MEDIUM".equals(level)) mediumRefreshed.add(cityKey);
                    }

                } catch (Exception ex) {
                    log.error("Error processing city {}", key, ex);
                }
            });
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // preserve interrupt status
                log.warn("Thread join interrupted for cityKey={}", t.getName());
            }
        }

        if (!hotRefreshed.isEmpty()) log.info("HOT refreshed: {}", hotRefreshed);
        if (!mediumRefreshed.isEmpty()) log.info("MEDIUM refreshed: {}", mediumRefreshed);

        if (hotRefreshed.isEmpty() && mediumRefreshed.isEmpty()) {
            log.info("No cities were refreshed as thresholds were not crossed. Scheduler completed.");
        } else {
            log.info("Scheduler completed with refreshed cities.");
        }
    }
}
