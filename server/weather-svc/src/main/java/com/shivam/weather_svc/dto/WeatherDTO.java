package com.shivam.weather_svc.dto;

import lombok.Data;

/**
 * Weather description.
 */
@Data
public class WeatherDTO {
    private int id;             // Weather condition ID
    private String main;        // Group of weather parameters (Rain, Clear, etc.)
    private String description; // Detailed description
    private String icon;        // Weather icon code
}
