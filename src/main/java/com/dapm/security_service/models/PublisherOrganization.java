package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.models.enums.Tier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
@Entity
@Table(name = "publisher_organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublisherOrganization {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private Tier tier;

}
