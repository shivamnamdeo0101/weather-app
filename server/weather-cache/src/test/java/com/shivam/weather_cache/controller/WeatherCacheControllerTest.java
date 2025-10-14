package com.shivam.weather_cache.controller;

import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.utils.WeatherUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherCacheController.class)
class WeatherCacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherCacheService cacheService;

    @Test
    void testGetWeather_cacheHit() throws Exception {
        Map<String, Object> data = Map.of("temp", 25);
        CacheResult result = new CacheResult(data, true);

        when(cacheService.getWeather("Paris")).thenReturn(result);

        mockMvc.perform(get("/api/weather-cache/forecast")
                        .param("city", "Paris"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Cache", "HIT"))
                .andExpect(jsonPath("$.temp").value(25));
    }

    @Test
    void testGetWeather_cacheMiss() throws Exception {
        Map<String, Object> data = Map.of("temp", 30);
        CacheResult result = new CacheResult(data, false);

        when(cacheService.getWeather("Berlin")).thenReturn(result);

        mockMvc.perform(get("/api/weather-cache/forecast")
                        .param("city", "Berlin"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Cache", "MISS"))
                .andExpect(jsonPath("$.temp").value(30));
    }
}
