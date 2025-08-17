package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.ProjectRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import  com.dapm.security_service.models.Project;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProjectDto {
    private UUID id;
    private String name;
    private String organizationName;
    private Set<String> roles;

    public ProjectDto(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.organizationName = project.getOrganization().getName();
        this.roles = project.getProjectRoles() != null
                ? project.getProjectRoles().stream().map(ProjectRole::getName).collect(Collectors.toSet())
                : Collections.emptySet();
    }
}
