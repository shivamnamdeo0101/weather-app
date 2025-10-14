package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.ForecastResponseDTO;
import com.shivam.weather_svc.dto.WeatherDTO;
import com.shivam.weather_svc.exception.ExternalApiException;
import com.shivam.weather_svc.utils.WeatherPrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WeatherServiceImplTest {

    private WeatherPrediction predictionService;
    private RestTemplate restTemplate;
    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        predictionService = mock(WeatherPrediction.class);
        restTemplate = mock(RestTemplate.class);
        weatherService = new WeatherServiceImpl(restTemplate, predictionService);

        // Inject private @Value fields via reflection
        setField(weatherService, "apiKey", "dummyKey");
        setField(weatherService, "apiUrl", "http://dummy.api");
        setField(weatherService, "cnt", "3");
        setField(weatherService, "units", "metric");
    }

    @Test
    void shouldReturnPredictionsForValidCity() {
        ForecastItemDTO item = new ForecastItemDTO();
        item.setWeather(List.of(new WeatherDTO()));
        ForecastResponseDTO responseDTO = new ForecastResponseDTO();
        responseDTO.setList(List.of(item));

        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenReturn(responseDTO);
        when(predictionService.generatePredictions(item))
                .thenReturn(List.of("Carry Umbrella"));

        List<ForecastItemDTO> result = weatherService.getThreeHourForecast("London");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of("Carry Umbrella"), result.get(0).getPredictions());

        verify(restTemplate, times(1)).getForObject(anyString(), eq(ForecastResponseDTO.class));
        verify(predictionService, times(1)).generatePredictions(item);
    }

    @Test
    void shouldThrowExceptionForEmptyCityName() {
        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("  "));
        assertEquals("City name cannot be empty.", ex.getMessage());
    }

    @Test
    void shouldReturnEmptyListForNullApiResponse() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenReturn(null);

        List<ForecastItemDTO> result = weatherService.getThreeHourForecast("London");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleHttpClientErrorException404() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("London"));
        assertEquals("City not found: London", ex.getMessage());
    }

    @Test
    void shouldHandleHttpClientErrorException400() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("London"));
        assertEquals("Bad request: Invalid city name or malformed query parameters.", ex.getMessage());
    }

    @Test
    void shouldHandleHttpClientErrorException401() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("London"));
        assertEquals("Unauthorized: Invalid or missing API key.", ex.getMessage());
    }

    @Test
    void shouldHandleResourceAccessExceptionTimeout() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenThrow(new ResourceAccessException("Timeout", new SocketTimeoutException()));

        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("London"));
        assertEquals("Weather service timed out. Please try again later.", ex.getMessage());
    }

    @Test
    void shouldHandleResourceAccessExceptionOther() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("London"));
        assertEquals("Weather service is currently unreachable. Please try again later.", ex.getMessage());
    }

    @Test
    void shouldHandleUnexpectedRuntimeException() {
        when(restTemplate.getForObject(anyString(), eq(ForecastResponseDTO.class)))
                .thenThrow(new RuntimeException("Unknown error"));

        ExternalApiException ex = assertThrows(ExternalApiException.class,
                () -> weatherService.getThreeHourForecast("London"));
        assertEquals("Unexpected internal error while fetching weather data.", ex.getMessage());
    }

    // Helper method to set private @Value fields
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
