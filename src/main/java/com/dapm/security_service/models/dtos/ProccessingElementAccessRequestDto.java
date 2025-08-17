package com.dapm.security_service.models.dtos;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProccessingElementAccessRequestDto {
    private UUID requestId;
    private String processingElement;
    private String pipelineName;
    private String requesterUsername;
    private String webhookUrl;
    private AccessRequestStatus status;

    public ProccessingElementAccessRequestDto(PipelineProcessingElementRequest request) {
        this.requestId = request.getId();
        this.processingElement = request.getPipelineNode().getTemplateId();
        this.pipelineName = request.getPipelineId().toString();
        this.requesterUsername = request.getRequesterInfo().getUsername();
        this.webhookUrl = request.getWebhookUrl();
        this.status = request.getStatus();
    }
}
