package com.shivam.weather_svc.exception;


import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Thrown when an external API call fails.
 */
@Getter
public class ExternalApiException extends RuntimeException {
    private final HttpStatusCode status;

    public ExternalApiException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }

}
