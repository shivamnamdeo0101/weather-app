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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomResponse<>(false,ex.getMessage(),null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomResponse<>(false,ex.getMessage(),null));
    }
}
