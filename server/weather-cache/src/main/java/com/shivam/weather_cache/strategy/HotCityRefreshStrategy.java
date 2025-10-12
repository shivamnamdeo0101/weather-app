package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component("HOT")
public class HotCityRefreshStrategy extends AbstractCityRefreshStrategy {

    @Value("${schedular.hot_hit_threshold}")
    private long hotHitThreshold;

    @Value("${schedular.hot_refresh_interval}")
    private long hotRefreshInterval;

    public HotCityRefreshStrategy(GenericRedisService redisService, WeatherSvcClient weatherSvcClient) {
        super(redisService, weatherSvcClient);
    }

    @Override
    protected long getHitThreshold() {
        return hotHitThreshold;
    }

    @Override
    protected long getRefreshInterval() {
        return hotRefreshInterval;
    }

    @Override
    protected String getLevelName() {
        return "HOT";
    }
}
