package com.shivam.weather_cache.utils;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    /**
     * Convert epoch milliseconds to formatted date-time string.
     * @param epochMilli milliseconds since epoch
     * @return formatted string, e.g., "2025-10-07 18:25:00"
     */
    public static String formatEpochMilli(long epochMilli) {
        return FORMATTER.format(Instant.ofEpochMilli(epochMilli));
    }
}