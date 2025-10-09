package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.enums.ProjectPermAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignProjectRolePermissionDto {
    private String projectName;
    private String roleName;
    private Set<ProjectPermAction> action;
}
