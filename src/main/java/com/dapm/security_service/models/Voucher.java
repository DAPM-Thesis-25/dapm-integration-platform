package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voucher")
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class Voucher {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. "BASIC-2025-ORG"

    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier; // FREE, BASIC, PREMIUM

    @Column(nullable = false)
    private boolean redeemed;

    private Instant redeemedAt;
}

