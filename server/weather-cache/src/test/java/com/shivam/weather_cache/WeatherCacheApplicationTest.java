package com.shivam.weather_cache;

import com.shivam.weather_cache.scheduler.WeatherCacheScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WeatherCacheApplicationTests {

    @Autowired
    private WeatherCacheScheduler scheduler;

    @Test
    void contextLoadsAndSchedulerIsInjected() {
        // Just ensures Spring context loads and scheduler bean exists
        assert scheduler != null;
    }
}
