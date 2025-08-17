package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Project;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ProjectRolesAssignmentDto {
    private String role = "";
}
