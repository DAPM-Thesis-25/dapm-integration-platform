package com.dapm.security_service.ingest_anonymization.utils;

import com.fasterxml.jackson.databind.JsonNode;
import communication.message.impl.event.Attribute;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FlattenOtherAttributeToJson {

    /**
     * Recursively flattens all fields in a JSON object into key → value attributes,
     * excluding the main Event fields like id, type, timestamp.
     *
     * @param node       The root JsonNode to flatten
     * @param parentKey  The prefix for nested fields
     * @param attributes The set to store generated attributes
     */
    private static final AttributeSanitizer sanitize= new AttributeSanitizer();
    public static void flatten(JsonNode node, String parentKey, Set<Attribute<?>> attributes) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = parentKey.isEmpty() ? entry.getKey() : parentKey + "." + entry.getKey();

                // Skip fields used in the main Event
                if (isCoreField(key)) continue;

                flatten(entry.getValue(), key, attributes);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                flatten(node.get(i), parentKey + "[" + i + "]", attributes);
            }
        } else {
            String value = node.asText("").replaceAll("=", ":").replaceAll("\n", " ").trim();
            if (!value.isEmpty()) {
                attributes.add(new Attribute<>(parentKey, sanitize.sanitize(value)));
            }
        }
    }

    // Only skip main Event fields
    private static boolean isCoreField(String key) {
        return key.equals("id") || key.equals("type") || key.equals("timestamp") || key.equals("meta.dt");
    }
}