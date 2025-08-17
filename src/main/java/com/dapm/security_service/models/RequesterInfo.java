package com.dapm.security_service.models;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
public class RequesterInfo {
    private UUID requesterId;
    private String username;
    private String organization;
    @Lob
    private String token;
}
