package com.shivam.weather_svc.exception;

/**
 * Thrown when a requested resource is not found.
 */
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }
}
