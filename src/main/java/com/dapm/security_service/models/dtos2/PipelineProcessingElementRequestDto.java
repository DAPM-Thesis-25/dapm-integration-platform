package com.dapm.security_service.models.dtos2;

import lombok.Data;


@Data
public class PipelineProcessingElementRequestDto {
    private String processingElement;
    private String pipelineName;
    private int requestedDurationHours;
    private String webhookUrl;
}




