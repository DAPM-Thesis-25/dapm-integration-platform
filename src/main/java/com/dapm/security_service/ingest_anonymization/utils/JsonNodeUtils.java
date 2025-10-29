package com.dapm.security_service.ingest_anonymization.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class JsonNodeUtils {

    /**
     * Returns a JsonNode at the given dot‐delimited path, or MissingNode if any segment is absent.
     */
    public static JsonNode getNodeByPath(JsonNode root, String path) {
        if (path == null || path.isEmpty()) {
            return root;
        }
        JsonNode current = root;
        for (String segment : path.split("\\.")) {
            if (current == null || current.isMissingNode()) {
                return MissingNode.getInstance();
            }
            current = current.get(segment);
        }
        return current == null ? MissingNode.getInstance() : current;
    }

    /**
     * Returns text at the given dot‐path, or the given default if missing or null.
     */
    public static String getTextByPath(JsonNode root, String path, String defaultValue) {
        JsonNode node = getNodeByPath(root, path);
        if (node.isMissingNode() || node.isNull()) {
            return defaultValue;
        }
        return node.asText(defaultValue);
    }

    // You can add other variants (e.g. getIntByPath, getBooleanByPath, etc.) here.
}
