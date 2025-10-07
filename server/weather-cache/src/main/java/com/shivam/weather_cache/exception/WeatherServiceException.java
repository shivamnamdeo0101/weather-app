package com.shivam.weather_cache.exception;

import org.springframework.http.HttpStatus;

public class WeatherServiceException extends RuntimeException {

    private final HttpStatus status;

    public WeatherServiceException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public WeatherServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public WeatherServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
