package com.shivam.weather_cache.utils;

import com.shivam.weather_cache.exception.BadRequestException;
import lombok.experimental.UtilityClass;

/**
 * Utility methods for city name validation and normalization.
 */

@UtilityClass
public final class CityUtils {

    /**
     * Trim and validate a city name. Throws BadRequestException when invalid.
     * Accepts only English letters (A-Z, a-z), spaces and hyphens.
     *
     * @param city raw city string
     * @return trimmed valid city
     */
    public static String validateAndTrimCity(String city) {
        if (city == null) {
            throw new BadRequestException("City parameter is required");
        }

        String trimmed = city.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException("City cannot be empty");
        }

        String pattern = "^[A-Za-z\\s-]+$";
        if (!trimmed.matches(pattern)) {
            throw new BadRequestException("City must contain only English letters, spaces or hyphens");
        }

        return trimmed;
    }
}
