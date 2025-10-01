package com.shivam.weather_svc.utils;
import lombok.experimental.UtilityClass;

/**
 * Centralized constants for the entire application.
 * Uses Lombok @UtilityClass for cleaner syntax.
 */
@UtilityClass
public class AppConstants {

    // ===========================
    // API Response Messages
    // ===========================
    public final class Messages {
        public static final String FORECAST_SUCCESS = "Forecast retrieved successfully";
        public static final String FORECAST_NOT_FOUND = "No forecast available for city: ";
        public static final String VALIDATION_FAILED = "Validation failed: ";
        public static final String INTERNAL_SERVER_ERROR = "Internal Server Error: ";
        public static final String CITY_NOT_FOUND = "City not found";
        public static final String OPENWEATHER_API_UNAUTHORIZED = "Unauthorized on GET request for openweather api";
    }

    // ===========================
    // JSON Response Keys
    // ===========================
    public final class Keys {
        public static final String SUCCESS = "success";
        public static final String MESSAGE = "message";
        public static final String DATA = "data";
        public static final String STATUS_CODE = "statusCode";
        public static final String TIMESTAMP = "timestamp";
    }

    // ===========================
    // Error Codes
    // ===========================
    public final class ErrorCodes {
        public static final String CITY_NOT_FOUND = "ERR001";
        public static final String VALIDATION_ERROR = "ERR002";
        public static final String INTERNAL_SERVER = "ERR003";
    }

    // ===========================
    // Weather-specific Constants
    // ===========================
    public final class Weather {
        public static final double MAX_TEMP_THRESHOLD = 40.0;
        public static final double MAX_WIND_SPEED = 10.0;
    }

    // ===========================
    // Default Values
    // ===========================
    public final class Defaults {
        public static final String DEFAULT_LOCALE = "en";
        public static final String DEFAULT_TIMEZONE = "UTC";
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
    }
}

