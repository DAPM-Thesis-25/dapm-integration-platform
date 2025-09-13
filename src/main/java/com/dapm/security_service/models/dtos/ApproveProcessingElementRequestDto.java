package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ApproveProcessingElementRequestDto {
    private UUID requestId;
//    private Integer allowedDurationHours;
    private AccessRequestStatus status;
}
