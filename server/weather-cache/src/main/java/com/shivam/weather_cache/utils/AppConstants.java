package com.shivam.weather_cache.utils;
import lombok.experimental.UtilityClass;

/**
 * Centralized constants for the entire application.
 * Uses Lombok @UtilityClass for cleaner syntax.
 */
@UtilityClass
public class AppConstants {

    // ===========================
    // Error Codes
    // ===========================
    public final class ErrorCodes {
        private ErrorCodes() {}
        public static final String VALIDATION_ERROR = "CAC001";
        public static final String INTERNAL_SERVER = "CAC002";
        public static final String WEATHER_SERVICE_ERROR = "CAC003";
    }

    // ===========================
    // Messages
    // ===========================
    public final class Messages {
        private Messages() {}

        // API Response Messages
        public static final String FORECAST_SUCCESS = "Weather forecast retrieved successfully.";
        public static final String FORECAST_NOT_FOUND = "No forecast data found for ";
        public static final String TOO_MANY_REQUEST = "Too many requests. Please wait before retrying.";
        public static final String CITY_NOT_FOUND = "City not found:";
        public static final String OPENWEATHER_API_UNAUTHORIZED = "Unauthorized access to OpenWeather API.";
        public static final String SERVICE_UNAVAILABLE = "Weather service is temporarily unavailable. Please try again later.";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error occurred.";
        public static final String BAD_REQUEST = "Invalid input provided.";
    }

    // ===========================
    // Headers
    // ===========================
    public final class Headers {
        private Headers() {}
        public static final String X_CACHE = "X-Cache";
        public static final String CACHE_HIT = "HIT";
        public static final String CACHE_MISS = "MISS";
    }

}