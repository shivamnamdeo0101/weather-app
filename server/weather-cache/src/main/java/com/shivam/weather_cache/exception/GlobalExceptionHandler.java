package com.shivam.weather_cache.exception;

import com.shivam.weather_cache.dto.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1️⃣ Weather SVC errors (429, 404, 502/503)
    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<CustomResponse<Object>> handleWeatherServiceException(WeatherServiceException ex) {
        log.error("Weather Service Exception: {}", ex.getMessage(), ex);
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    // 2️⃣ Endpoint not found → 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CustomResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler found for request: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        String message = String.format("Endpoint not found: %s. Please check the URL.", ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomResponse<>(false, message, null));
    }

    // 3️⃣ Missing or invalid query parameter → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponse<Object>> handleMissingRequestParam(
            MissingServletRequestParameterException ex) {
        log.warn("Missing required query parameter: {}", ex.getParameterName());
        String message = String.format("Missing or invalid query parameter: %s", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, message, null));
    }

    // 3b️⃣ Explicit BadRequestException → 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomResponse<Object>> handleBadRequest(BadRequestException ex) {
        log.warn("BadRequest: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    // 3c️⃣ Bean validation failures → 400
    @ExceptionHandler({ ConstraintViolationException.class, MethodArgumentNotValidException.class })
    public ResponseEntity<CustomResponse<Object>> handleValidationExceptions(Exception ex) {
        log.warn("Validation failure: {}", ex.getMessage());
        String message = "Validation failed for the request";
        // Try extract more specific message for common cases
        if (ex instanceof ConstraintViolationException cve) {
            if (!cve.getConstraintViolations().isEmpty()) {
                message = cve.getConstraintViolations().iterator().next().getMessage();
            }
        } else if (ex instanceof MethodArgumentNotValidException manv) {
            message = manv.getBindingResult().getAllErrors().stream()
                    .findFirst()
                    .map(err -> err.getDefaultMessage())
                    .orElse(message);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, message, null));
    }

    // 4️⃣ Illegal argument → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponse<>(false, ex.getMessage(), null));
    }

    // 5️⃣ Fallback → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomResponse<>(false,
                        "An unexpected error occurred. Please try again later.",
                        null));
    }
}
