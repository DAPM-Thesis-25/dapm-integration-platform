package com.dapm.security_service.ingest_anonymization.processingStages;

import com.dapm.security_service.ingest_anonymization.config.SpringContext;
import com.dapm.security_service.ingest_anonymization.mongo.AnonymizationMappingService;
import com.dapm.security_service.ingest_anonymization.utils.AppConstants;
import com.dapm.security_service.ingest_anonymization.utils.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pipeline.processingelement.Configuration;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pipeline.processingelement.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class AnonymizationProcess {
    private final String dataSourceId;
    private final List<String> pseudoFields;
    private final List<String> supFields;
    private final String uniqueField;
    private final AnonymizationMappingService mappingService;

    private AnonymizationProcess(String dataSourceId,
                                 List<String> pseu,
                                 List<String> sup,
                                 String uniq,
                                 AnonymizationMappingService mappingService) {
        this.dataSourceId    = dataSourceId;
        this.pseudoFields    = pseu;
        this.supFields       = sup;
        this.uniqueField     = uniq;
        this.mappingService  = mappingService;
    }

    @SuppressWarnings("unchecked")
    public static AnonymizationProcess getAnonymizationConfig(Configuration configuration) {
        // 1) Grab the top‐level Map<String,Object> that Jackson has already produced
        Map<String,Object> cfgMap = configuration.getConfiguration();

        // 2) Pull out the "anonymization" section
        Object rawAnon = cfgMap.get("anonymization");
        if (rawAnon == null) {
            throw new IllegalArgumentException(
                    "Configuration does not contain an 'anonymization' section: " + cfgMap
            );
        }
        if (!(rawAnon instanceof Map)) {
            throw new IllegalArgumentException(
                    "'anonymization' is not a JSON object (Map) but was: "
                            + rawAnon.getClass().getSimpleName()
            );
        }
        Map<String,Object> anonMap = (Map<String,Object>) rawAnon;

        // 3) Extract "dataSourceId" (must be a String) — **we used to look at cfgMap, but now pull from anonMap**
        Object rawDS = anonMap.get("dataSourceId");
        if (rawDS == null) {
            throw new IllegalArgumentException(
                    "'anonymization' object does not contain 'dataSourceId': " + anonMap
            );
        }
        if (!(rawDS instanceof String)) {
            throw new IllegalArgumentException(
                    "'anonymization.dataSourceId' is not a String, but was: "
                            + rawDS.getClass().getSimpleName()
            );
        }
        String dataSourceId = (String) rawDS;

        // 4) Extract "pseudonymization": must be a List<String>
        Object rawPseudo = anonMap.get("pseudonymization");
        if (rawPseudo == null) {
            throw new IllegalArgumentException(
                    "'anonymization' object does not contain 'pseudonymization': " + anonMap
            );
        }
        if (!(rawPseudo instanceof List)) {
            throw new IllegalArgumentException(
                    "'anonymization.pseudonymization' is not a List but was: "
                            + rawPseudo.getClass().getSimpleName()
            );
        }
        List<Object> pseudoListRaw = (List<Object>) rawPseudo;
        List<String> pseudonymizationFields = new ArrayList<>();
        for (Object o : pseudoListRaw) {
            if (!(o instanceof String)) {
                throw new IllegalArgumentException(
                        "Each item in 'anonymization.pseudonymization' must be a String, "
                                + "but encountered: " + o.getClass().getSimpleName()
                );
            }
            pseudonymizationFields.add((String) o);
        }

        // 5) Extract "suppression": must be a List<String>
        Object rawSuppress = anonMap.get("suppression");
        if (rawSuppress == null) {
            throw new IllegalArgumentException(
                    "'anonymization' object does not contain 'suppression': " + anonMap
            );
        }
        if (!(rawSuppress instanceof List)) {
            throw new IllegalArgumentException(
                    "'anonymization.suppression' is not a List but was: "
                            + rawSuppress.getClass().getSimpleName()
            );
        }
        List<Object> suppressListRaw = (List<Object>) rawSuppress;
        List<String> suppressionFields = new ArrayList<>();
        for (Object o : suppressListRaw) {
            if (!(o instanceof String)) {
                throw new IllegalArgumentException(
                        "Each item in 'anonymization.suppression' must be a String, "
                                + "but encountered: " + o.getClass().getSimpleName()
                );
            }
            suppressionFields.add((String) o);
        }

        // 6) Extract "uniqueField": must be a String
        Object rawUnique = anonMap.get("uniqueField");
        if (rawUnique == null) {
            throw new IllegalArgumentException(
                    "'anonymization' object does not contain 'uniqueField': " + anonMap
            );
        }
        if (!(rawUnique instanceof String)) {
            throw new IllegalArgumentException(
                    "'anonymization.uniqueField' is not a String but was: "
                            + rawUnique.getClass().getSimpleName()
            );
        }
        String uniqueField = (String) rawUnique;

        // 7) Look up the mappingService bean from the Spring context
        AnonymizationMappingService mappingSvc =
                SpringContext.getBean(AnonymizationMappingService.class);

        // 8) Build and return the AnonymizationProcess
        return new AnonymizationProcess(
                dataSourceId,
                pseudonymizationFields,
                suppressionFields,
                uniqueField,
                mappingSvc
        );
    }

    public JsonNode apply(JsonNode json) {
        if (pseudoFields.isEmpty() && supFields.isEmpty()) {
            return json;
        }

        // keep a raw copy
        JsonNode raw = json.deepCopy();
        ObjectNode node = (ObjectNode) json;

        // only proceed if uniqueField exists
        JsonNode uniqNode = getNodeByPath(raw, uniqueField);
        if (uniqueField != null && !uniqNode.isMissingNode()) {
            String mappingId = UUID.randomUUID().toString();

            // 1) pseudonymize each field
            for (String field : pseudoFields) {
                JsonNode origNode = getNodeByPath(raw, field);
                String originalValue = origNode.isMissingNode() ? "" : origNode.asText();
                String pseudoValue   = mappingService.pseudonym(dataSourceId, uniqueField, field, raw);
                setNodeByPath(node, field, pseudoValue);
            }

            // 2) suppress each field
            for (String field : supFields) {
                removeNodeByPath(node, field);
            }

            // 3) build wrapper
            ObjectNode wrapper = JsonNodeFactory.instance.objectNode();
            wrapper.put(AppConstants.MAPPING_Table_ID, mappingId);
            wrapper.set(AppConstants.Raw_Data, raw);
            wrapper.set(AppConstants.Anonymized_Data, node);
            mappingService.saveRawDataAnonymization(dataSourceId, wrapper);

            String mappingRef = AppConstants.ANONYMIZE_STATUS_TRUE + ";" + dataSourceId + ";" + mappingId;
            node.put(AppConstants.MAPPING_Table_REFERENCE, mappingRef);
        }
        return node;
    }

    /** Walks a dotted path via JsonNodeUtils. */
    private JsonNode getNodeByPath(JsonNode json, String path) {
        return JsonNodeUtils.getNodeByPath(json, path);
    }

    /** Sets a nested value, creating intermediate objects if needed. */
    private void setNodeByPath(ObjectNode root, String path, String value) {
        String[] parts = path.split("\\.");
        ObjectNode node = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = node.get(parts[i]);
            if (!(child instanceof ObjectNode)) {
                child = JsonNodeFactory.instance.objectNode();
                node.set(parts[i], child);
            }
            node = (ObjectNode) child;
        }
        node.put(parts[parts.length - 1], value);
    }

    /** Removes a nested field if present. */
    private void removeNodeByPath(ObjectNode root, String path) {
        String[] parts = path.split("\\.");
        ObjectNode node = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = node.path(parts[i]);
            if (!(child instanceof ObjectNode)) {
                return;
            }
            node = (ObjectNode) child;
        }
        node.remove(parts[parts.length - 1]);
    }
}

