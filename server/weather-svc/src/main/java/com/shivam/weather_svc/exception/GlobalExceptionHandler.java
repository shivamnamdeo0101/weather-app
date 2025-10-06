package com.shivam.weather_svc.exception;

import com.shivam.weather_svc.dto.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Centralized exception handling for all controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<CustomResponse<Object>> handleExternalApiException(ExternalApiException ex) {
        log.error("External API Exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomResponse<Object>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponse<Object>> handleMissingRequestParam(
            org.springframework.web.bind.MissingServletRequestParameterException ex) {
        log.warn("Missing required parameter: {}", ex.getParameterName());
        String message = "Missing required query parameter: " + ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, message, null));
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<CustomResponse<Object>> handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException ex) {
        log.warn("No handler found for request: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomResponse<>(false, "No handler found for this endpoint. Please check the API path.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomResponse<>(false, "An unexpected error occurred. Please try again later.", null));
    }
}
