package com.dapm.security_service.ingest_anonymization.utils;

public class AttributeSanitizer {
    public static String sanitize(String raw) {
        return raw.replaceAll("[:=]", "_")  // Replace JXES-breaking characters
                .replaceAll("\\n", " ")   // Remove newlines
                .replaceAll("[\\r\\t]", " ")
                .trim();
    }
}
