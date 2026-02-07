package com.miracle.smart_ecommerce_api_v1.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 */
public final class DateUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATETIME_FORMAT);

    private DateUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Format LocalDateTime to default format
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format LocalDateTime to ISO format
     */
    public static String formatDateTimeISO(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(ISO_FORMATTER);
    }

    /**
     * Parse string to LocalDateTime using default format
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * Get current UTC time
     */
    public static LocalDateTime nowUTC() {
        return ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
    }

    /**
     * Get current local time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}

