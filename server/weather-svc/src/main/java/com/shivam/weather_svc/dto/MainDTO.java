package com.shivam.weather_svc.dto;

import lombok.Data;

/**
 * Contains temperature info for forecast item.
 */
@Data
public class MainDTO {
    private double temp;       // Current temp
    private double feels_like; // Feels like temp
    private double temp_min;   // Minimum temp
    private double temp_max;   // Maximum temp
    private int pressure;      // Atmospheric pressure
    private int sea_level;     // Sea level pressure
    private int grnd_level;    // Ground level pressure
    private int humidity;      // Humidity percentage
    private double temp_kf;    // Internal parameter
}
