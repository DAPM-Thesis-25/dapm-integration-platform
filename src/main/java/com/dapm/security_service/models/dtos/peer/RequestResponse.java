package com.dapm.security_service.models.dtos.peer;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class RequestResponse {
    private UUID requestId;
    private AccessRequestStatus requestStatus;
    private String token;
}
