package com.dapm.security_service.models;

import jakarta.persistence.*;
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
//    @Lob
//    @Basic(fetch = FetchType.EAGER)
//    private String token;
    @Column(length = 4096)
    private String token;

}
