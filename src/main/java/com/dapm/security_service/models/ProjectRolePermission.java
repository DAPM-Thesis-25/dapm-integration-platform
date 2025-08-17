package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "project_role_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "role_id", "permission_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRolePermission {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ProjectRole role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private ProjectPermission permission;

    // Add this version field to enable optimistic locking:
    @Version
    private Long version;
}
