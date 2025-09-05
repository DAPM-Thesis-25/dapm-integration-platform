package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequest {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // The user who made the access request.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(name = "details", length = 1000)
    private String details;

    // ABAC attribute: requested duration (in hours)
    @Column(name = "duration_in_hours")
    private Long durationInHours;

    @Column(name = "request_time", nullable = false)
    private Instant requestTime;

    @Column(name = "decision_time")
    private Instant decisionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccessRequestStatus status;

    @PrePersist
    protected void onCreate() {
        this.requestTime = Instant.now();
    }
}
