// ** TODO: move the token to vault
package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_pe_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineProcessingElementRequest {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_pe_id")
    private ProcessingElement processingElement;

    @Column(name = "instance_number")
    private Integer instanceNumber;

    @Column(name = "pipeline_id", nullable = false)
    private String pipelineName;

    @Embedded
    private RequesterInfo requesterInfo;

    @Column(name = "requested_duration_hours")
    private int requestedDurationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccessRequestStatus status;

    @Column(name = "allowedDurationHours", nullable = true)
    private Integer allowedDurationHours;

    @Column(name = "approval_token", length = 4096)
    private String approvalToken;

    @Column(name = "decision_time")
    private Instant decisionTime;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_pe_instance_id")
    private PipelineProcessingElementInstance pipelineProcessingElementInstance;

}
