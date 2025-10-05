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
        private ErrorMessages() {}
        public static final String VALIDATION_ERROR = "Validation error";
        public static final String INTERNAL_SERVER = "Internal server error";
        public static final String WEATHER_SERVICE_ERROR = "Weather service error";
    }

}