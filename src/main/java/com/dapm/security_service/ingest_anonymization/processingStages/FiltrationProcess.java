package com.dapm.security_service.ingest_anonymization.processingStages;

import com.dapm.security_service.ingest_anonymization.utils.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pipeline.processingelement.Configuration;

import java.util.Map;

public class FiltrationProcess {

    private final Map<String, Object> filters;

    private FiltrationProcess(Map<String, Object> filters) {
        this.filters = filters;
    }

    @SuppressWarnings("unchecked")
    public static FiltrationProcess getFilterConfig(Configuration configuration) {
        // 1) Grab the top‐level map that Jackson already created
        Map<String,Object> cfgMap = configuration.getConfiguration();

        // 2) Pull out the "filters" entry
        Object rawFilters = cfgMap.get("filters");
        if (rawFilters == null) {
            throw new IllegalArgumentException(
                    "Configuration does not contain a 'filters' section: " + cfgMap
            );
        }
        if (!(rawFilters instanceof Map)) {
            throw new IllegalArgumentException(
                    "'filters' is not a JSON object/map: " + rawFilters.getClass().getSimpleName()
            );
        }

        // 3) Cast it down to a Map<String,Object> (or Map<String,String> if you know all values are strings)
        Map<String,Object> parsedFilters = (Map<String,Object>) rawFilters;

        // 4) Construct your FiltrationProcess using the same Map-based constructor you already had
        return new FiltrationProcess(parsedFilters);
    }
    /**
     * Returns true only if every key in `filters` matches the JSON at that path.
     * Uses null‐safe lookups and avoids any direct .get(...).textValue() calls.
     */
    public boolean shouldPass(JsonNode eventJson) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String path = entry.getKey();
            Object expectedValue = entry.getValue();
            JsonNode actualNode = JsonNodeUtils.getNodeByPath(eventJson, path);

            if (actualNode.isMissingNode() || actualNode.isNull()) {
                return false;
            }

            // 1. Boolean filter (no change)
            if (expectedValue instanceof Boolean) {
                boolean actualBool = actualNode.asBoolean(false);
                if (actualBool != (Boolean) expectedValue) {
                    return false;
                }
                continue;
            }

            // 2. List filter (support ["ruwiki", "enwiki"])
            if (expectedValue instanceof Iterable) {
                String actualText = actualNode.asText(null);
                boolean matched = false;
                for (Object value : (Iterable<?>) expectedValue) {
                    if (actualText != null && actualText.equals(value.toString())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
                continue;
            }

            // 3. String/primitive filter (old behavior)
            String actualText = actualNode.asText(null);
            if (actualText == null || !actualText.equals(expectedValue.toString())) {
                return false;
            }
        }
        return true;
    }

}
