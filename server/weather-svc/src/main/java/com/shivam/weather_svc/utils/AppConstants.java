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
        private Messages() {}
        public static final String FORECAST_SUCCESS = "Forecast retrieved successfully";
        public static final String FORECAST_NOT_FOUND = "No forecast available for city: ";
        public static final String VALIDATION_FAILED = "Validation failed: ";
        public static final String INTERNAL_SERVER_ERROR = "Internal Server Error: ";
        public static final String CITY_NOT_FOUND = "City not found";
        public static final String OPENWEATHER_API_UNAUTHORIZED = "Unauthorized on GET request for openweather api";
        public static final String TOO_MANY_REQUEST= "Too many request! Try after 60 seconds";
    }

    // ===========================
    // JSON Response Keys
    // ===========================
    public final class Keys {
        private Keys() {}
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
        private ErrorCodes() {}
        public static final String CITY_NOT_FOUND = "SVC001";
        public static final String VALIDATION_ERROR = "SVC002";
        public static final String INTERNAL_SERVER = "SVC003";
        public static final String OPENWEATHER_API_UNAUTHORIZED = "SVC004";
        public static final String TOO_MANY_REQUEST = "SVC005";
        public static final String WEATHER_API_CALL_FAILED = "SVC006";
    }

    // ===========================
    // Weather-specific Constants
    // ===========================
    public final class Weather {
        private Weather() {}
        public static final double MAX_TEMP_THRESHOLD = 40.0;
        public static final double MAX_WIND_SPEED = 10.0;

        public final class Conditions {
            private Conditions() {}
            public static final String RAIN = "Rain";
            public static final String THUNDERSTORM = "Thunderstorm";
        }

        public final class Predictions {
            private Predictions() {}
            public static final String CARRY_UMBRELLA = "Carry umbrella";
            public static final String STORM_WARNING = "Don't step out! A Storm is brewing!";
            public static final String USE_SUNSCREEN = "Use sunscreen lotion";
            public static final String TOO_WINDY = "It's too windy, watch out!";
        }
    }

    // ===========================
    // Default Values
    // ===========================
    public final class Defaults {
        private Defaults() {}
        public static final String DEFAULT_LOCALE = "en";
        public static final String DEFAULT_TIMEZONE = "UTC";
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
    }
}

