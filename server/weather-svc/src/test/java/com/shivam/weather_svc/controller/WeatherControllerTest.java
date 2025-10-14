package com.shivam.weather_svc.controller;

import com.shivam.weather_svc.dto.CustomResponse;
import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.service.WeatherService;
import com.shivam.weather_svc.utils.SlidingWindowRateLimiter;
import com.shivam.weather_svc.utils.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private SlidingWindowRateLimiter rateLimiter;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return forecast successfully when city is valid and rate limit not exceeded")
    void testGetForecast_Success() {
        String city = "London";
        ForecastItemDTO forecastItem = new ForecastItemDTO();
        List<ForecastItemDTO> forecastList = List.of(forecastItem);

        when(rateLimiter.tryConsume()).thenReturn(true);
        when(weatherService.getThreeHourForecast(city)).thenReturn(forecastList);

        ResponseEntity<CustomResponse<List<ForecastItemDTO>>> response = weatherController.getForecast(city);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(AppConstants.Messages.FORECAST_SUCCESS, response.getBody().getMessage());
        assertEquals(forecastList, response.getBody().getData());

        verify(rateLimiter, times(1)).tryConsume();
        verify(weatherService, times(1)).getThreeHourForecast(city);
    }

    @Test
    @DisplayName("Should return 429 when rate limit exceeded")
    void testGetForecast_RateLimitExceeded() {
        String city = "Paris";
        when(rateLimiter.tryConsume()).thenReturn(false);

        ResponseEntity<CustomResponse<List<ForecastItemDTO>>> response = weatherController.getForecast(city);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals(AppConstants.Messages.TOO_MANY_REQUEST, response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(rateLimiter, times(1)).tryConsume();
        verify(weatherService, never()).getThreeHourForecast(anyString());
    }

    @Test
    @DisplayName("Should return 404 when forecast data is empty")
    void testGetForecast_CityNotFound() {
        String city = "UnknownCity";

        when(rateLimiter.tryConsume()).thenReturn(true);
        when(weatherService.getThreeHourForecast(city)).thenReturn(Collections.emptyList());

        ResponseEntity<CustomResponse<List<ForecastItemDTO>>> response = weatherController.getForecast(city);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains(AppConstants.Messages.CITY_NOT_FOUND));
        assertNull(response.getBody().getData());

        verify(rateLimiter, times(1)).tryConsume();
        verify(weatherService, times(1)).getThreeHourForecast(city);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when city is empty")
    void testGetForecast_EmptyCity() {
        String city = "  ";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> weatherController.getForecast(city));

        assertEquals("City name cannot be empty.", exception.getMessage());

        verify(rateLimiter, never()).tryConsume();
        verify(weatherService, never()).getThreeHourForecast(anyString());
    }
}
