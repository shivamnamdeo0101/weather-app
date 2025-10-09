package com.shivam.weather_cache.exception;

import com.shivam.weather_cache.dto.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom WeatherServiceException thrown by downstream weather services.
     * Logs 4xx as warnings and 5xx as errors (without stack traces for clean logs).
     */
    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<CustomResponse<Object>> handleWeatherServiceException(WeatherServiceException ex) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;

        if (status.is4xxClientError()) {
            log.warn("Weather service client error ({}): {}", status.value(), ex.getMessage());
        } else {
            log.error("Weather service server error ({}): {}", status.value(), ex.getMessage());
        }

        return ResponseEntity.status(status)
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    /**
     * Handles 404s when no endpoint matches the requested path.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CustomResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler found for request: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        String message = String.format("Endpoint not found: %s. Please check the URL.", ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomResponse<>(false, message, null));
    }

    /**
     * Handles missing or invalid query parameters (e.g. ?city= is missing).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponse<Object>> handleMissingRequestParam(MissingServletRequestParameterException ex) {
        log.warn("Missing required query parameter: {}", ex.getParameterName());
        String message = String.format("Missing or invalid query parameter: %s", ex.getParameterName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, message, null));
    }
}
