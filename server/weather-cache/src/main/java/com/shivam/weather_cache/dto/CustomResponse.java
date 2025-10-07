package com.shivam.weather_cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic, reusable API response wrapper.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
