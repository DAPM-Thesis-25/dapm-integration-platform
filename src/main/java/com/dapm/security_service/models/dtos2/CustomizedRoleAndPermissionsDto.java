package com.dapm.security_service.models.dtos2;

import com.dapm.security_service.models.enums.ProjectPermAction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomizedRoleAndPermissionsDto {
    private String roleName;
    private String projectName;
    private List<ProjectPermAction> permissions;
}
