package com.dapm.security_service.models.dtos2.designpipeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class DesignProcessingElementDto {
    private String templateID;
    private JsonNode configuration;
}
