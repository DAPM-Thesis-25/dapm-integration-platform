package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.OrgPermAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "org_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgPermission {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_action", nullable = false)
    private OrgPermAction action;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "orgRole_id", nullable = false)
    private OrgRole orgRole;
}
