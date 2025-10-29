package com.dapm.security_service.ingest_anonymization.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import communication.message.impl.Trace;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;

public class JXESConverter {

    public static String convertTraceToJXES(Trace trace) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode tracesArray = mapper.createArrayNode();

        ObjectNode traceNode = mapper.createObjectNode();
        traceNode.put("traceID", trace.getCaseID());

        ArrayNode eventsArray = mapper.createArrayNode();
        for (Event e : trace) {
            ObjectNode eventNode = mapper.createObjectNode();
            // core event fields
            eventNode.put("activity", e.getActivity());
            eventNode.put("timestamp", e.getTimestamp());
            eventNode.put("caseId", e.getCaseID());

            // extract special metadata attributes to top-level of eventNode
            for (Attribute<?> attr : e.getAttributes()) {
                String name = attr.getName();
                Object value = attr.getValue();
                if ("mappingTableID".equals(name)) {
                    eventNode.put(name, value.toString());
                } else if ("anonymize".equals(name) && value instanceof Boolean) {
                    eventNode.put(name, (Boolean) value);
                } else if ("dataSourceID".equals(name)) {
                    eventNode.put(name, value.toString());
                }
            }

            // now build the attributes block, skipping the special ones
            ObjectNode attrNode = mapper.createObjectNode();
            for (Attribute<?> attr : e.getAttributes()) {
                String name = attr.getName();
                if ("mappingTableID".equals(name) || "anonymize".equals(name) || "dataSourceID".equals(name)) {
                    continue;
                }
                Object value = attr.getValue();
                String valueWithType;
                if (value instanceof String) {
                    String safeString = value.toString()
                            .replace("\"", "'")
                            .replace("=", ":")
                            .replace("\n", " ")
                            .trim();
                    valueWithType = "string:" + safeString;
                } else if (value instanceof Integer) {
                    valueWithType = "int:" + value;
                } else if (value instanceof Boolean) {
                    valueWithType = "boolean:" + value;
                } else {
                    valueWithType = "string:" + value.toString().replace("\"", "'").trim();
                }
                attrNode.put(name, valueWithType);
            }
            eventNode.set("attributes", attrNode);
            eventsArray.add(eventNode);
        }

        traceNode.set("events", eventsArray);
        tracesArray.add(traceNode);
        root.set("traces", tracesArray);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "{}";
        }
    }
}
