package com.shivam.weather_svc.dto;

import lombok.Data;

import java.util.List;

/**
 * Represents a single forecast item (3-hour interval) from the API.
 */
@Data
public class ForecastItemDTO {
    private String dt_txt;
    private MainDTO main;
    private List<WeatherDTO> weather;
    private WindDTO wind;

    // Add this field to store predictions per forecast item
    private List<String> predictions;
}
