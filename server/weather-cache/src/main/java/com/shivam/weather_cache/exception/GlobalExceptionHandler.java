package com.shivam.weather_cache.exception;

import com.shivam.weather_cache.dto.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1️⃣ Weather SVC errors (4xx/5xx)
    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<CustomResponse<Object>> handleWeatherServiceException(WeatherServiceException ex) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Weather Service server error ({}): {}", status.value(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    // 2️⃣ Endpoint not found → 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CustomResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.error("No handler found for request: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        String message = String.format("Endpoint not found: %s. Please check the URL.", ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomResponse<>(false, message, null));
    }

    // 3️⃣ Missing or invalid query parameter → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponse<Object>> handleMissingRequestParam(
            MissingServletRequestParameterException ex) {
        log.error("Missing required query parameter: {}", ex.getParameterName());
        String message = String.format("Missing or invalid query parameter: %s", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, message, null));
    }

}