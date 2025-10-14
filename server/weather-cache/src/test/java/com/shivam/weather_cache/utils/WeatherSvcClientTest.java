package com.shivam.weather_cache.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WeatherSvcClientTest {

    @Mock
    private RestTemplate restTemplate;

    private WeatherSvcClient weatherSvcClient;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        weatherSvcClient = new WeatherSvcClient(restTemplate);

        // Use reflection to inject svcUrl
        java.lang.reflect.Field field = WeatherSvcClient.class.getDeclaredField("svcUrl");
        field.setAccessible(true);
        field.set(weatherSvcClient, "http://fake-weather-service");
    }


    @Test
    void testFetchWeatherData_success() {
        String city = "Paris";

        Map<String, Object> fakeResponse = Map.of("temp", 25, "condition", "Sunny");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(fakeResponse, HttpStatus.OK);

        // Mock RestTemplate exchange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenReturn(responseEntity);

        Map<String, Object> result = weatherSvcClient.fetchWeatherData(city);

        assertNotNull(result);
        assertEquals(25, result.get("temp"));
        assertEquals("Sunny", result.get("condition"));

        // Verify RestTemplate called with correct URL
        verify(restTemplate, times(1)).exchange(
                contains("city=Paris"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        );
    }

    @Test
    void testFetchWeatherData_httpException() {
        String city = "Berlin";

        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()
        )).thenThrow(exception);

        HttpStatusCodeException ex = assertThrows(HttpStatusCodeException.class,
                () -> weatherSvcClient.fetchWeatherData(city));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    @Test
    void testFallbackWeatherData() {
        String city = "London";

        Map<String, Object> fallback = weatherSvcClient.fallbackWeatherData(city, new RuntimeException("Service down"));

        assertNotNull(fallback);
        assertTrue(fallback.isEmpty());
    }
}
