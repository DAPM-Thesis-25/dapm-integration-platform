package com.dapm.security_service.ingest_anonymization.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class TimestampConverterISO {

    /**
     * Converts a variety of timestamp formats to a standard ISO 8601 string (UTC).
     * Accepts Unix seconds, Unix milliseconds, or ISO 8601 strings.
     *
     * @param rawTimestamp the timestamp (can be String, Long, Integer)
     * @return ISO 8601 formatted timestamp, or current time if invalid
     */
    public static String toISO(Object rawTimestamp) {
        try {
            if (rawTimestamp instanceof Long || rawTimestamp instanceof Integer) {
                long ts = ((Number) rawTimestamp).longValue();

                // 13-digit = milliseconds, 10-digit = seconds
                Instant instant = (ts > 9999999999L)
                        ? Instant.ofEpochMilli(ts)
                        : Instant.ofEpochSecond(ts);

                return DateTimeFormatter.ISO_INSTANT.format(instant);
            }

            // Parse string value
            String str = rawTimestamp.toString().trim();

            if (str.matches("^\\d{10,}$")) {
                long ts = Long.parseLong(str);
                Instant instant = (ts > 9999999999L)
                        ? Instant.ofEpochMilli(ts)
                        : Instant.ofEpochSecond(ts);
                return DateTimeFormatter.ISO_INSTANT.format(instant);
            }

            // Try ISO string
            Instant instant = Instant.parse(str);
            return DateTimeFormatter.ISO_INSTANT.format(instant);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to convert timestamp to ISO: " + rawTimestamp);
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        }
    }
}