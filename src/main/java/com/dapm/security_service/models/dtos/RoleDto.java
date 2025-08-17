package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Permission;
import com.dapm.security_service.models.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RoleDto {
    private UUID id;
    private String name;
    private OrganizationDto organization;
    private Set<String> permissions;
    public RoleDto(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.organization = new OrganizationDto(role.getOrganization()) ;
        this.permissions = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
