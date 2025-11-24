package com.shivam.weather_cache.bdd;

import com.shivam.weather_cache.controller.WeatherCacheController;
import com.shivam.weather_cache.dto.CacheResult;
import com.shivam.weather_cache.service.GenericRedisService;
import com.shivam.weather_cache.service.WeatherCacheService;
import com.shivam.weather_cache.strategy.CacheRefreshStrategy;
import com.shivam.weather_cache.strategy.CacheRefreshStrategyFactory;
import com.shivam.weather_cache.utils.AppConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.weather_cache.utils.WeatherSvcClient;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CacheOperationsStepDefinitions {

    private WeatherCacheController weatherCacheController;
    private WeatherCacheService weatherCacheService;
    private GenericRedisService redisService;
    private WeatherSvcClient weatherSvcClient;
    private CacheRefreshStrategyFactory strategyFactory;
    private ObjectMapper objectMapper;
    private ResponseEntity<Map<String, Object>> response;
    private String cityName;

    @Before
    public void setUp() {
        redisService = mock(GenericRedisService.class);
        objectMapper = new ObjectMapper();
        weatherSvcClient = mock(WeatherSvcClient.class);
        strategyFactory = mock(CacheRefreshStrategyFactory.class);
        
        weatherCacheService = new com.shivam.weather_cache.service.WeatherCacheServiceImpl(
                redisService,
                objectMapper,
                weatherSvcClient,
                strategyFactory
        );
        
        weatherCacheController = new WeatherCacheController(weatherCacheService);
    }

    @Given("the weather cache service is available")
    public void the_weather_cache_service_is_available() {
        // Service is initialized in setUp
    }

    @Given("the cache contains weather data for {string}")
    public void the_cache_contains_weather_data_for(String city) {
        this.cityName = city;
        String key = AppConstants.Cache.WEATHER_KEY_PREFIX + city.toLowerCase();
        
        Map<String, Object> cachedData = new HashMap<>();
        cachedData.put("city", city);
        cachedData.put("temp", 25);
        cachedData.put("description", "Sunny");
        
        when(redisService.getAndUpdateMeta(key)).thenReturn(cachedData);
        
        CacheRefreshStrategy strategy = mock(CacheRefreshStrategy.class);
        when(strategyFactory.getStrategy(key)).thenReturn(strategy);
        when(strategy.refreshIfRequired(key)).thenReturn(false);
    }

    @Given("the cache does not contain weather data for {string}")
    public void the_cache_does_not_contain_weather_data_for(String city) {
        this.cityName = city;
        String key = AppConstants.Cache.WEATHER_KEY_PREFIX + city.toLowerCase();
        when(redisService.getAndUpdateMeta(key)).thenReturn(null);
    }

    @Given("the weather service returns data for {string}")
    public void the_weather_service_returns_data_for(String city) {
        Map<String, Object> svcData = new HashMap<>();
        svcData.put("city", city);
        svcData.put("temp", 30);
        svcData.put("description", "Cloudy");
        
        when(weatherSvcClient.fetchWeatherData(city)).thenReturn(svcData);
    }

    @When("I request weather forecast for {string}")
    public void i_request_weather_forecast_for(String city) {
        this.cityName = city;
        this.response = weatherCacheController.getWeather(city);
    }

    @Then("I should receive a successful response with X-Cache header {string}")
    public void i_should_receive_a_successful_response_with_x_cache_header(String cacheStatus) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        HttpHeaders headers = response.getHeaders();
        assertTrue(headers.containsKey(AppConstants.Headers.X_CACHE));
        assertEquals(cacheStatus, headers.getFirst(AppConstants.Headers.X_CACHE));
    }

    @And("the response should contain weather data")
    public void the_response_should_contain_weather_data() {
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertTrue(response.getBody().containsKey("city"));
    }

    @And("the data should be saved to cache")
    public void the_data_should_be_saved_to_cache() {
        String key = AppConstants.Cache.WEATHER_KEY_PREFIX + cityName.toLowerCase();
        verify(redisService, times(1)).saveWithMeta(eq(key), any(), eq(false), eq(1L));
    }
}

