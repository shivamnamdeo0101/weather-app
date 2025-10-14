package com.shivam.weather_cache.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeUtilsTest {

    @Test
    void testFormatEpochMilli() {
        // Given: a known epoch milliseconds value
        long epochMilli = 1697183100000L; // corresponds to 2023-10-13 08:05:00 UTC

        // When: formatted using DateTimeUtils
        String formatted = DateTimeUtils.formatEpochMilli(epochMilli);

        // Then: it should match expected string in system default timezone
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        String expected = formatter.format(Instant.ofEpochMilli(epochMilli));

        assertEquals(expected, formatted);
    }

    @Test
    void testFormatEpochMilliWithZero() {
        long epochMilli = 0L; // epoch start
        String formatted = DateTimeUtils.formatEpochMilli(epochMilli);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        String expected = formatter.format(Instant.ofEpochMilli(epochMilli));

        assertEquals(expected, formatted);
    }
}
