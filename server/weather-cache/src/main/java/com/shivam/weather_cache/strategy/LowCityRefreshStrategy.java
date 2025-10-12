package com.shivam.weather_cache.strategy;

import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("LOW")
public class LowCityRefreshStrategy implements CacheRefreshStrategy {

    @Override
    public boolean refreshIfRequired(String cityKey) {
        log.debug("☁️ LOW city '{}' does not need refresh", cityKey);
        return false;
    }
}
