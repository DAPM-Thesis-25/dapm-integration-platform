package com.dapm.security_service.ingest_anonymization.mongo;

import com.dapm.security_service.ingest_anonymization.utils.AppConstants;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.dapm.security_service.ingest_anonymization.utils.JsonNodeUtils.getTextByPath;

@Service
public class AnonymizationMappingService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AnonymizationMappingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private String collectionForMappingTable(String dataSourceId) {
        return AppConstants.Mapping_Table_For_ + dataSourceId;
    }

    public void saveRawData(String dataSourceId, JsonNode json) {
        String coll = AppConstants.Ingested_Raw_Data_For_ + dataSourceId;
        Document doc = Document.parse(json.toString());
        mongoTemplate.save(doc, coll);
    }

    public void saveRawDataAnonymization(String dataSourceId, JsonNode wrapper) {
        String coll = collectionForMappingTable(dataSourceId);
        Document doc = Document.parse(wrapper.toString());
        mongoTemplate.save(doc, coll);
    }

    public Optional<Document> findExistingPseudonymValue(String dataSourceId, String uniqueField, String uniqueFieldValue, String field, String fieldValue) {
        String coll = collectionForMappingTable(dataSourceId);
        // start with the uniqueField criterion
        Criteria crit = Criteria.where(AppConstants.Raw_Data + "." + uniqueField)
                .is(uniqueFieldValue);

        // only add the second clause if it's a *different* JSON key
        if (!uniqueField.equals(field)) {
            crit = crit.and(AppConstants.Raw_Data + "." + field)
                    .is(fieldValue);
        }

        Query q = Query.query(crit);
        return Optional.ofNullable(
                mongoTemplate.findOne(q, Document.class, coll)
        );
    }

    public String pseudonym(String dataSourceId, String uniqueFieldName, String fieldName, JsonNode rawData) {
        if (uniqueFieldName == null || fieldName == null) {
            return "";
        }
        String uniqueFieldValue = getTextByPath(rawData, uniqueFieldName, "");
        String fieldValue       = getTextByPath(rawData, fieldName, "");
        // 1) Check if we've already anonymized this exact value
        Optional<Document> existing = findExistingPseudonymValue(dataSourceId, uniqueFieldName, uniqueFieldValue, fieldName, fieldValue);
        if (existing.isPresent()) {
            Document anon = existing.get()
                    .get(AppConstants.Anonymized_Data, Document.class);
            if (anon != null && anon.containsKey(fieldName)) {
                // reuse token
                System.out.println("Duplicate value found on " + fieldName + " = "+fieldValue+", pseudo= " + anon.getString(fieldName));
                return anon.getString(fieldName);
            }
        }

        // 2) Otherwise generate a brand-new token (avoiding collisions)
        String coll = collectionForMappingTable(dataSourceId);
        String safeField = fieldName.trim().replaceAll("[^A-Za-z0-9]+", "_");
        String token;
        do {
            int rnd = ThreadLocalRandom.current().nextInt(1, 1_000_000);
            token = safeField + "_" + rnd;
        } while (mongoTemplate.exists(
                Query.query(Criteria.where(AppConstants.Anonymized_Data + "." + fieldName)
                        .is(token)),
                Document.class,
                coll
        ));

        return token;
    }
    public Optional<String> deanonymize(String dataSourceId, String uniqueFieldName, String fieldName, JsonNode raw) {
        String coll = collectionForMappingTable(dataSourceId);
        return null;
    }
}

