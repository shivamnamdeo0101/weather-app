package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component("MEDIUM")
public class MediumCityRefreshStrategy extends AbstractCityRefreshStrategy {

    @Value("${schedular.medium_hit_threshold}")
    private long mediumHitThreshold;

    @Value("${schedular.medium_refresh_interval}")
    private long mediumRefreshInterval;

    public MediumCityRefreshStrategy(GenericRedisService redisService, WeatherSvcClient weatherSvcClient) {
        super(redisService, weatherSvcClient);
    }

    @Override
    protected long getHitThreshold() {
        return mediumHitThreshold;
    }

    @Override
    protected long getRefreshInterval() {
        return mediumRefreshInterval;
    }

    @Override
    protected String getLevelName() {
        return "🌤 MEDIUM";
    }
}
