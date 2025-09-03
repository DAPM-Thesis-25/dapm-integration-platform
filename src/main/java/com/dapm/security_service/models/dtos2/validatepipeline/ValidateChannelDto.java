package com.dapm.security_service.models.dtos2.validatepipeline;

import com.dapm.security_service.models.dtos2.designpipeline.DesignChannelsDto;
import com.dapm.security_service.models.dtos2.designpipeline.DesignProcessingElementDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ValidateChannelDto {
    private ValidateProcessingElementDto publisher;
    private List<SubscriberDto> subscribers; // <-- FIXED

    @Data
    public static class SubscriberDto {
        @JsonProperty("processing element")
        private ValidateProcessingElementDto processingElement;

        private Integer portNumber;
    }
}

