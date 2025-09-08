package com.dapm.security_service.models.dtos2;

import com.dapm.security_service.models.RequesterInfo;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO for sending PipelineNodeRequest data to OrgB.
 */
@Data
public class PipelineProcessingElementRequestOutboundDto {
    private UUID id;
    private String processingElementName;
    private RequesterInfo requesterInfo;
    private String pipelineName;
    private int requestedDurationHours;
    private String webhookUrl;
    private int instanceNumber;
}
