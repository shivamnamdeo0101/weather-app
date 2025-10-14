package com.shivam.weather_cache.utils;

import com.shivam.weather_cache.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CityUtilsTest {

    @Test
    void testValidateAndTrimCity_validCity() {
        String input = "  New York  ";
        String expected = "New York";
        String actual = CityUtils.validateAndTrimCity(input);
        assertEquals(expected, actual);
    }

    @Test
    void testValidateAndTrimCity_validCityWithHyphen() {
        String input = "Los-Angeles";
        String expected = "Los-Angeles";
        String actual = CityUtils.validateAndTrimCity(input);
        assertEquals(expected, actual);
    }

    @Test
    void testValidateAndTrimCity_nullCity_throwsException() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            CityUtils.validateAndTrimCity(null);
        });
        assertEquals("City parameter is required", exception.getMessage());
    }

    @Test
    void testValidateAndTrimCity_emptyCity_throwsException() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            CityUtils.validateAndTrimCity("   ");
        });
        assertEquals("City cannot be empty", exception.getMessage());
    }

    @Test
    void testValidateAndTrimCity_invalidCharacters_throwsException() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            CityUtils.validateAndTrimCity("New York123");
        });
        assertEquals("City must contain only English letters, spaces or hyphens", exception.getMessage());
    }

    @Test
    void testValidateAndTrimCity_multipleSpaces() {
        String input = "  San   Francisco  ";
        String expected = "San   Francisco"; // internal spaces are preserved
        String actual = CityUtils.validateAndTrimCity(input);
        assertEquals(expected, actual);
    }
}
