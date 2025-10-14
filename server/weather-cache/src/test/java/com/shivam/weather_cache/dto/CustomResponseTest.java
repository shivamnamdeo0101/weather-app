package com.shivam.weather_cache.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomResponseTest {

    @Test
    void testAllArgsConstructor() {
        String message = "Success";
        Integer data = 100;
        boolean success = true;

        CustomResponse<Integer> response = new CustomResponse<>(success, message, data);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        CustomResponse<String> response = new CustomResponse<>();
        response.setSuccess(false);
        response.setMessage("Failed");
        response.setData("ErrorData");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Failed");
        assertThat(response.getData()).isEqualTo("ErrorData");
    }

    @Test
    void testBuilder() {
        CustomResponse<String> response = CustomResponse.<String>builder()
                .success(true)
                .message("Built")
                .data("BuilderData")
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Built");
        assertThat(response.getData()).isEqualTo("BuilderData");
    }
}
