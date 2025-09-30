package com.shivam.weather_svc.dto;

import lombok.Data;

/**
 * Wind information.
 */
@Data
public class WindDTO {
    private double speed; // Wind speed in m/s
    private int deg;      // Wind direction degrees
    private double gust;  // Gust speed
}
