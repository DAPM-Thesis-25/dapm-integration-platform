package com.dapm.security_service.ingest_anonymization.processingStages;

import pipeline.processingelement.Configuration;
import java.util.Map;

/**
 * Utility class for extracting the "eventSource.url" from a Configuration.
 */
public class EventSourceConfig {

    @SuppressWarnings("unchecked")
    public static String getEventSourceUrl(Configuration configuration) {
        // 1) Grab the top‐level map that Jackson already created
        Map<String,Object> cfgMap = configuration.getConfiguration();

        // 2) Pull out the "eventSource" entry
        Object rawEventSource = cfgMap.get("eventSource");
        if (rawEventSource == null) {
            throw new IllegalArgumentException(
                    "Configuration does not contain an 'eventSource' section: " + cfgMap
            );
        }
        if (!(rawEventSource instanceof Map)) {
            throw new IllegalArgumentException(
                    "'eventSource' is not a JSON object/map, but was: "
                            + rawEventSource.getClass().getSimpleName()
            );
        }
        Map<String,Object> eventSourceMap = (Map<String,Object>) rawEventSource;

        // 3) From that map, pull out "url"
        Object rawUrl = eventSourceMap.get("url");
        if (rawUrl == null) {
            throw new IllegalArgumentException(
                    "'eventSource' object does not contain a 'url' field: " + eventSourceMap
            );
        }
        if (!(rawUrl instanceof String)) {
            throw new IllegalArgumentException(
                    "'eventSource.url' is not a string, but was: "
                            + rawUrl.getClass().getSimpleName()
            );
        }

        return (String) rawUrl;
    }
}

