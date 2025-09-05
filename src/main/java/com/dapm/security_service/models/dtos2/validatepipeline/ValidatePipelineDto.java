package com.dapm.security_service.models.dtos2.validatepipeline;

import com.dapm.security_service.models.dtos2.designpipeline.DesignChannelsDto;
import com.dapm.security_service.models.dtos2.designpipeline.DesignProcessingElementDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ValidatePipelineDto {
    @JsonProperty("processing elements")
    private List<ValidateProcessingElementDto> processingElements;

    private List<ValidateChannelDto> channels;
}
