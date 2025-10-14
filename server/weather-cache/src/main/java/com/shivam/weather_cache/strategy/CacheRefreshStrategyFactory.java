package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CacheRefreshStrategyFactory {

    private final HotCityRefreshStrategy hotStrategy;
    private final MediumCityRefreshStrategy mediumStrategy;
    private final LowCityRefreshStrategy lowStrategy;
    private final GenericRedisService redisService;

    public CacheRefreshStrategyFactory(HotCityRefreshStrategy hotStrategy,
                                       MediumCityRefreshStrategy mediumStrategy,
                                       LowCityRefreshStrategy lowStrategy,
                                       GenericRedisService redisService) {
        this.hotStrategy = hotStrategy;
        this.mediumStrategy = mediumStrategy;
        this.lowStrategy = lowStrategy;
        this.redisService = redisService;
    }

    public CacheRefreshStrategy getStrategy(String cityKey) {
        Map<Object, Object> meta = redisService.getMeta(cityKey);
        long hits = 0;
        if (meta != null) {
            hits = meta.get("hits") == null ? 0 : Long.parseLong(meta.get("hits").toString());
        }

        if (hits >= hotStrategy.getHitThreshold()) return hotStrategy;
        if (hits >= mediumStrategy.getHitThreshold()) return mediumStrategy;
        return lowStrategy;
    }
}
