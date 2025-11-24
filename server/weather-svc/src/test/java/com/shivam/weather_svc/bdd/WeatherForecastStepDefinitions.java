package com.shivam.weather_svc.bdd;

import com.shivam.weather_svc.controller.WeatherController;
import com.shivam.weather_svc.dto.CustomResponse;
import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.service.WeatherService;
import com.shivam.weather_svc.utils.SlidingWindowRateLimiter;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WeatherForecastStepDefinitions {

    private WeatherController weatherController;
    private WeatherService weatherService;
    private SlidingWindowRateLimiter rateLimiter;
    private ResponseEntity<CustomResponse<List<ForecastItemDTO>>> response;
    private String cityName;
    private IllegalArgumentException exception;

    @Before
    public void setUp() {
        weatherService = mock(WeatherService.class);
        rateLimiter = mock(SlidingWindowRateLimiter.class);
        weatherController = new WeatherController(weatherService, rateLimiter);
    }

    @Given("the weather service is available")
    public void the_weather_service_is_available() {
        // Service is initialized in setUp
    }

    @Given("a valid city name {string}")
    public void a_valid_city_name(String city) {
        this.cityName = city;
        when(rateLimiter.tryConsume()).thenReturn(true);
        ForecastItemDTO forecastItem = new ForecastItemDTO();
        when(weatherService.getThreeHourForecast(city)).thenReturn(List.of(forecastItem));
    }

    @Given("the rate limit has been exceeded")
    public void the_rate_limit_has_been_exceeded() {
        when(rateLimiter.tryConsume()).thenReturn(false);
    }

    @Given("an invalid city name {string}")
    public void an_invalid_city_name(String city) {
        this.cityName = city;
        when(rateLimiter.tryConsume()).thenReturn(true);
        when(weatherService.getThreeHourForecast(city)).thenReturn(Collections.emptyList());
    }

    @Given("an empty city name")
    public void an_empty_city_name() {
        this.cityName = "   ";
    }

    @When("I request the weather forecast")
    public void i_request_the_weather_forecast() {
        try {
            this.response = weatherController.getForecast(cityName);
        } catch (IllegalArgumentException e) {
            this.exception = e;
        }
    }

    @When("I request the weather forecast for {string}")
    public void i_request_the_weather_forecast_for(String city) {
        this.cityName = city;
        this.response = weatherController.getForecast(city);
    }

    @Then("I should receive a successful response with forecast data")
    public void i_should_receive_a_successful_response_with_forecast_data() {
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertFalse(response.getBody().getData().isEmpty());
        verify(rateLimiter, times(1)).tryConsume();
        verify(weatherService, times(1)).getThreeHourForecast(cityName);
    }

    @Then("I should receive a rate limit error response")
    public void i_should_receive_a_rate_limit_error_response() {
        assertNotNull(response);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(rateLimiter, times(1)).tryConsume();
        verify(weatherService, never()).getThreeHourForecast(anyString());
    }

    @Then("I should receive a city not found error response")
    public void i_should_receive_a_city_not_found_error_response() {
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(rateLimiter, times(1)).tryConsume();
        verify(weatherService, times(1)).getThreeHourForecast(cityName);
    }

    @Then("I should receive a bad request error response")
    public void i_should_receive_a_bad_request_error_response() {
        assertNotNull(exception);
        assertEquals("City name cannot be empty.", exception.getMessage());
        verify(rateLimiter, never()).tryConsume();
        verify(weatherService, never()).getThreeHourForecast(anyString());
    }
}

