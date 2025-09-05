package com.dapm.security_service.models.dtos2.designpipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DesignPipelineDto {
    private String pipelineName;
    private String projectName;

    @JsonProperty("processing elements")
    private List<DesignProcessingElementDto> processingElements;

    private List<DesignChannelsDto> channels;
}
