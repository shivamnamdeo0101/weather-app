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
    // Messages (Grouped by HTTP Status)
    // ===========================
    public final class Messages {
        private Messages() {}

        // --- 2XX SUCCESS ---
        public final class Success {
            private Success() {}
            public static final String FORECAST_SUCCESS = "Weather forecast retrieved successfully.";
        }

        // --- 4XX CLIENT ERRORS ---
        public final class ClientError {
            private ClientError() {}
            public static final String BAD_REQUEST = "Invalid input provided."; // 400
            public static final String OPENWEATHER_API_UNAUTHORIZED = "Unauthorized access to OpenWeather API."; // 401
            public static final String CITY_NOT_FOUND = "City not found:"; // 404 (For specific application logic/messages)
            public static final String FORECAST_NOT_FOUND = "No forecast data found for "; // 404 (Specific to forecast data availability)
            public static final String TOO_MANY_REQUEST = "Too many requests. Please wait before retrying."; // 429
        }

        // --- 5XX SERVER ERRORS ---
        public final class ServerError {
            private ServerError() {}
            public static final String SERVICE_UNAVAILABLE = "Weather service is temporarily unavailable. Please try again later."; // 502 / 503
            public static final String INTERNAL_SERVER_ERROR = "Internal server error occurred."; // 500
        }
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