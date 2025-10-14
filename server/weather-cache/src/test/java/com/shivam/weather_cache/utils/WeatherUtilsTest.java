package com.shivam.weather_cache.utils;

import com.shivam.weather_cache.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherUtilsTest {

    @Test
    void validateAndTrimCity_validCity_shouldReturnTrimmed() {
        String input = "  New York  ";
        String result = WeatherUtils.validateAndTrimCity(input);
        assertThat(result).isEqualTo("New York");
    }

    @Test
    void validateAndTrimCity_nullCity_shouldThrow() {
        assertThatThrownBy(() -> WeatherUtils.validateAndTrimCity(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("City parameter is required");
    }

    @Test
    void validateAndTrimCity_emptyCity_shouldThrow() {
        assertThatThrownBy(() -> WeatherUtils.validateAndTrimCity("   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("City cannot be empty");
    }

    @Test
    void validateAndTrimCity_invalidCharacters_shouldThrow() {
        assertThatThrownBy(() -> WeatherUtils.validateAndTrimCity("New York123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("City must contain only English letters, spaces or hyphens");

        assertThatThrownBy(() -> WeatherUtils.validateAndTrimCity("Paris!"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("City must contain only English letters, spaces or hyphens");

        assertThatThrownBy(() -> WeatherUtils.validateAndTrimCity("Berlin@2025"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("City must contain only English letters, spaces or hyphens");
    }

    @Test
    void validateAndTrimCity_cityWithHyphen_shouldPass() {
        String input = "Rio-de-Janeiro";
        String result = WeatherUtils.validateAndTrimCity(input);
        assertThat(result).isEqualTo("Rio-de-Janeiro");
    }

    @Test
    void validateAndTrimCity_cityWithSpaces_shouldPass() {
        String input = "San Francisco";
        String result = WeatherUtils.validateAndTrimCity(input);
        assertThat(result).isEqualTo("San Francisco");
    }
}
