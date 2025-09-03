package com.dapm.security_service.models.dtos2.validatepipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ValidateProcessingElementDto {
    private String organizationID;
    private String hostURL;
    private String templateID;
    private Set<String> inputs;
    private String output;
    private Integer instanceNumber;
    private JsonNode configuration;
}
