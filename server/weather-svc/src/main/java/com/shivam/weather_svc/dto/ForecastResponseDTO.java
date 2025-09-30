package com.shivam.weather_svc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * ForecastResponseDTO: Maps the full API response from OpenWeatherMap
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponseDTO {

    private String cod;
    private int message;
    private int cnt;

    // This is the list of forecast items returned by the API
    private List<ForecastItemDTO> list;

    private CityDTO city;
}
