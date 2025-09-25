package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.ProjectRolePermission;
import com.dapm.security_service.models.enums.ProjectPermAction;
import com.dapm.security_service.models.enums.ProjectScope;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRolePermissionDto {
    private String projectName;
    private String roleName;
    private Set<ProjectPermAction> action;

    public ProjectRolePermissionDto(ProjectRolePermission prp) {
        this.projectName = prp.getProject().getName();
        this.roleName = prp.getRole().getName();
//        this.action = prp.getPermission().getAction();
        this.action = Set.of(prp.getPermission().getAction());
    }
}
