package com.dapm.security_service.models.dtos2.designpipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DesignChannelsDto {
    private DesignProcessingElementDto publisher;
    private List<SubscriberDto> subscribers;

    @Data
    public static class SubscriberDto {
        @JsonProperty("processing element")
        private DesignProcessingElementDto processingElement;

        private Integer portNumber;
    }
}

