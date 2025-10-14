package com.shivam.weather_svc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test for WeatherSvcApplication.
 * Verifies that the Spring application context loads successfully.
 */
@SpringBootTest
class WeatherSvcApplicationTest {

    @Test
    @DisplayName("Application context should load without errors")
    void contextLoads() {
        // If the application context fails to start, this test will fail automatically.
    }

    @Test
    @DisplayName("Main method should run without throwing exceptions")
    void mainMethodRunsSuccessfully() {
        String[] args = {};
        WeatherSvcApplication.main(args);
        // No assertion needed; if main throws, the test will fail
    }
}
