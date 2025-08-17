package com.dapm.security_service.models;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "partner_organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerOrganization {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
