package com.shivam.weather_svc.utils;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import com.shivam.weather_svc.dto.MainDTO;
import com.shivam.weather_svc.dto.WeatherDTO;
import com.shivam.weather_svc.dto.WindDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherPredictionTest {

    private WeatherPrediction weatherPrediction;

    @BeforeEach
    void setUp() {
        weatherPrediction = new WeatherPrediction();
    }

    @Test
    void shouldSuggestUmbrellaForRain() {
        ForecastItemDTO item = createForecast("Rain", 25, 5);
        List<String> predictions = weatherPrediction.generatePredictions(item);

        assertTrue(predictions.contains(AppConstants.Weather.Predictions.CARRY_UMBRELLA));
    }

    @Test
    void shouldWarnForThunderstorm() {
        ForecastItemDTO item = createForecast("Thunderstorm", 25, 5);
        List<String> predictions = weatherPrediction.generatePredictions(item);

        assertTrue(predictions.contains(AppConstants.Weather.Predictions.STORM_WARNING));
    }

    @Test
    void shouldSuggestSunscreenForHighTemp() {
        ForecastItemDTO item = createForecast("Clear", AppConstants.Weather.MAX_TEMP_THRESHOLD + 5, 5);
        List<String> predictions = weatherPrediction.generatePredictions(item);

        assertTrue(predictions.contains(AppConstants.Weather.Predictions.USE_SUNSCREEN));
    }

    @Test
    void shouldWarnIfWindIsTooHigh() {
        ForecastItemDTO item = createForecast("Clear", 25, AppConstants.Weather.MAX_WIND_SPEED + 2);
        List<String> predictions = weatherPrediction.generatePredictions(item);

        assertTrue(predictions.contains(AppConstants.Weather.Predictions.TOO_WINDY));
    }

    @Test
    void shouldCombineMultiplePredictions() {
        ForecastItemDTO item = createForecast("Rain", AppConstants.Weather.MAX_TEMP_THRESHOLD + 5, AppConstants.Weather.MAX_WIND_SPEED + 2);
        List<String> predictions = weatherPrediction.generatePredictions(item);

        assertEquals(3, predictions.size());
        assertTrue(predictions.contains(AppConstants.Weather.Predictions.CARRY_UMBRELLA));
        assertTrue(predictions.contains(AppConstants.Weather.Predictions.USE_SUNSCREEN));
        assertTrue(predictions.contains(AppConstants.Weather.Predictions.TOO_WINDY));
    }

    // Helper method to create ForecastItemDTO
    private ForecastItemDTO createForecast(String weatherMain, double tempMax, double windSpeed) {
        ForecastItemDTO item = new ForecastItemDTO();

        WeatherDTO weatherDTO = new WeatherDTO();
        weatherDTO.setMain(weatherMain);
        item.setWeather(List.of(weatherDTO));

        MainDTO mainDTO = new MainDTO();
        mainDTO.setTemp_max(tempMax);
        item.setMain(mainDTO);

        WindDTO windDTO = new WindDTO();
        windDTO.setSpeed(windSpeed);
        item.setWind(windDTO);

        return item;
    }
}
