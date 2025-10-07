package com.shivam.weather_cache.utils;

import com.shivam.weather_cache.exception.BadRequestException;
import org.jetbrains.annotations.NotNull;

public class WeatherUtils {

    private WeatherUtils() {
        // private constructor to prevent instantiation
    }

    /**
     * Trims the city string and validates it.
     *
     * @param city the city name input
     * @return trimmed and validated city
     */
    public static @NotNull String validateAndTrimCity(String city) {
        if (city == null) {
            throw new BadRequestException("City parameter is required");
        }

        String trimmed = city.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException("City cannot be empty");
        }

        // Only allow English letters (A-Z, a-z), spaces and hyphens
        String pattern = "^[A-Za-z\\s-]+$";
        if (!trimmed.matches(pattern)) {
            throw new BadRequestException("City must contain only English letters, spaces or hyphens");
        }

        return trimmed;
    }
}
