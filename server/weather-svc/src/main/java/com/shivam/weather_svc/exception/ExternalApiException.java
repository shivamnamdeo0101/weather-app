package com.shivam.weather_svc.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Custom exception representing external API call failures or network issues.
 */
@Getter
public class ExternalApiException extends RuntimeException {
    private final HttpStatusCode status;

    public ExternalApiException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }
}
