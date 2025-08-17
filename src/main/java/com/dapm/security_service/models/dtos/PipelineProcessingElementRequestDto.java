package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PipelineProcessingElementRequestDto {
    private String processingElement;
    private String piplineName;
    private int requestedExecutionCount;
    private int requestedDurationHours;
    private String webhookUrl;
}




