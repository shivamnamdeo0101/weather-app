package com.shivam.weather_svc.dto;

import lombok.Data;

/**
 * Latitude and longitude of city.
 */
@Data
public class CoordDTO {
    private double lat;
    private double lon;
}
