package com.shivam.weather_svc.service;

import com.shivam.weather_svc.dto.ForecastItemDTO;
import java.util.List;

/**
 * WeatherService defines operations to fetch and process
 * weather forecast data from external weather APIs.
 *
 * <p>This interface provides methods for retrieving forecast data
 * and performing business logic enhancements like predictions
 * or post-processing.</p>
 */
public interface WeatherService {

    /**
     * Fetches 3-hour interval forecast data for a given city.
     *
     * @param cityName the name of the city to fetch weather for
     * @return a list of forecast items with predictions
     * @throws com.shivam.weather_svc.exception.ExternalApiException if the API call fails or city is invalid
     */
    List<ForecastItemDTO> getThreeHourForecast(String cityName);
}
