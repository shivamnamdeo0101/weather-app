package com.shivam.weather_svc.dto;

import lombok.Data;

/**
 * City information.
 */
@Data
public class CityDTO {
    private long id;
    private String name;
    private CoordDTO coord;
    private String country;
    private long population;
    private int timezone;   // Seconds offset from UTC
    private long sunrise;   // UNIX timestamp
    private long sunset;    // UNIX timestamp
}
