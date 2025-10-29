package com.dapm.security_service.ingest_anonymization.processingStages;


import com.dapm.security_service.ingest_anonymization.utils.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
public class AttributeMappingProcess {
    private final String caseIdField;
    private final String activityField;
    private final String timestampField;

    public AttributeMappingProcess(String caseIdField,
                                   String activityField,
                                   String timestampField) {
        this.caseIdField   = caseIdField;
        this.activityField = activityField;
        this.timestampField= timestampField;
    }
    @SuppressWarnings("unchecked")
    public static AttributeMappingProcess getAttributeMappingConfig(Configuration configuration) {
        // 1) Grab the top‐level Map<String,Object> that Jackson has already produced
        Map<String,Object> cfgMap = configuration.getConfiguration();
        // 2) Pull out the "attributeMapping" section
        Object rawAttrMap = cfgMap.get("attributeMapping");
        if (rawAttrMap == null) {
            throw new IllegalArgumentException(
                    "Configuration does not contain an 'attributeMapping' section: " + cfgMap
            );
        }
        if (!(rawAttrMap instanceof Map)) {
            throw new IllegalArgumentException(
                    "'attributeMapping' is not a JSON object (Map) but was: "
                            + rawAttrMap.getClass().getSimpleName()
            );
        }
        Map<String,Object> attrMap = (Map<String,Object>) rawAttrMap;
        // 3) Extract "caseId" (must be a String)
        Object rawCaseId = attrMap.get("caseId");
        if (rawCaseId == null) {
            throw new IllegalArgumentException(
                    "'attributeMapping' object does not contain 'caseId': " + attrMap
            );
        }
        if (!(rawCaseId instanceof String)) {
            throw new IllegalArgumentException(
                    "'attributeMapping.caseId' is not a String but was: "
                            + rawCaseId.getClass().getSimpleName()
            );
        }
        String caseIdField = (String) rawCaseId;
        // 4) Extract "activity" (must be a String)
        Object rawActivity = attrMap.get("activity");
        if (rawActivity == null) {
            throw new IllegalArgumentException(
                    "'attributeMapping' object does not contain 'activity': " + attrMap
            );
        }
        if (!(rawActivity instanceof String)) {
            throw new IllegalArgumentException(
                    "'attributeMapping.activity' is not a String but was: "
                            + rawActivity.getClass().getSimpleName()
            );
        }
        String activityField = (String) rawActivity;
        // 5) Extract "timeStamp" (must be a String)
        Object rawTimeStamp = attrMap.get("timeStamp");
        if (rawTimeStamp == null) {
            throw new IllegalArgumentException(
                    "'attributeMapping' object does not contain 'timeStamp': " + attrMap
            );
        }
        if (!(rawTimeStamp instanceof String)) {
            throw new IllegalArgumentException(
                    "'attributeMapping.timeStamp' is not a String but was: "
                            + rawTimeStamp.getClass().getSimpleName()
            );
        }
        String timeStampField = (String) rawTimeStamp;
        // 6) Build and return the AttributeMappingProcess
        return new AttributeMappingProcess(caseIdField, activityField, timeStampField);
    }
    public Event extractEvent(JsonNode json) {
        // 1) core fields
        String caseId   = JsonNodeUtils.getTextByPath(json, caseIdField,   "unknown_case");
        String activity = JsonNodeUtils.getTextByPath(json, activityField, "unknown_activity");
        String timestamp= JsonNodeUtils.getTextByPath(json, timestampField, "unknown_timestamp");
        // 2) strip core fields from a fresh copy
        if (!(json instanceof ObjectNode)) {
            throw new IllegalArgumentException("Expected JSON root to be an object");
        }
        ObjectNode copy = ((ObjectNode) json).deepCopy();
        removeByPath(copy, caseIdField);
        removeByPath(copy, activityField);
        removeByPath(copy, timestampField);
//        // 3) convert everything left into Attributes
        Set<Attribute<?>> attrs = buildAttributes(copy);
        return new Event(caseId, activity, timestamp, attrs);
    }
    private void removeByPath(ObjectNode root, String path) {
        String[] parts = path.split("\\.");
        ObjectNode parent = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = parent.get(parts[i]);
            if (!(child instanceof ObjectNode)) {
                return; // nothing to remove
            }
            parent = (ObjectNode) child;
        }
        parent.remove(parts[parts.length - 1]);
    }
    private Set<Attribute<?>> buildAttributes(JsonNode node) {
        Set<Attribute<?>> out = new LinkedHashSet<>();
        node.fieldNames().forEachRemaining(name -> {
            JsonNode child = node.get(name);
            if (child.isObject()) {
                // nested → recurse
                Set<Attribute<?>> nested = buildAttributes(child);
                Map<String,Attribute<?>> map = nested.stream()
                        .collect(Collectors.toMap(Attribute::getName, Function.identity()));
                out.add(new Attribute<>(name, null, map));
            } else if (child.isArray()) {
                // array → either list of primitives or index‐keyed nested
                List<Object> list = new ArrayList<>();
                Map<String,Attribute<?>> map = new LinkedHashMap<>();
                for (int i = 0; i < child.size(); i++) {
                    JsonNode el = child.get(i);
                    if (el.isObject()) {
                        Set<Attribute<?>> elNested = buildAttributes(el);
                        Map<String,Attribute<?>> em = elNested.stream()
                                .collect(Collectors.toMap(Attribute::getName, Function.identity()));
                        map.put(String.valueOf(i), new Attribute<>(String.valueOf(i), null, em));
                    } else {
                        list.add(extractPrimitive(el));
                    }
                }
                if (!map.isEmpty()) {
                    out.add(new Attribute<>(name, null, map));
                } else {
                    out.add(new Attribute<>(name, list));
                }
            } else {
                // primitive → linear
                out.add(new Attribute<>(name, extractPrimitive(child)));
            }
        });
        return out;
    }
    private Object extractPrimitive(JsonNode n) {
        if (n.isNumber())  return n.numberValue();
        if (n.isBoolean()) return n.booleanValue();
        return n.asText();
    }
}
