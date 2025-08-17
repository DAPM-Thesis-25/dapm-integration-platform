package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.AssignProjectRolePermissionDto;
import com.dapm.security_service.models.dtos.ProjectRolePermissionDto;
import com.dapm.security_service.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-role-permissions")
@RequiredArgsConstructor
public class ProjectRolePermissionController {

    private final ProjectRolePermissionRepository projectRolePermissionRepository;
    private final ProjectRepository projectRepository;
    private final ProjectsRolesRepository projectRoleRepository;
    private final ProjPermissionRepository projPermissionRepository;
    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @GetMapping
    public List<ProjectRolePermissionDto> getPermissions(@RequestParam String projectName) {
        Project project = projectRepository.findByName(projectName)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return projectRolePermissionRepository.findByProjectId(project.getId())
                .stream()
                .map(ProjectRolePermissionDto::new)
                .toList();
    }


    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @PostMapping
    public ResponseEntity<ProjectRolePermissionDto> assignPermission(@RequestBody AssignProjectRolePermissionDto request) {
        Project project = projectRepository.findByName(request.getProjectName())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ProjectRole role = project.getProjectRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(request.getRoleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role not found in project"));
        ProjectPermission permission = projPermissionRepository.findByAction(request.getAction());
        if (permission == null) {
            throw new IllegalArgumentException("Permission not found");
        }

        var existing = projectRolePermissionRepository.findByProjectAndPermissionAndRole(project, permission, role);
        if (existing.isPresent()) {
            return ResponseEntity.ok(new ProjectRolePermissionDto(existing.get()));
        }

        var newMapping = ProjectRolePermission.builder()
                .id(UUID.randomUUID())
                .project(project)
                .role(role)
                .permission(permission)
                .build();
        newMapping = projectRolePermissionRepository.save(newMapping);
        return ResponseEntity.ok(new ProjectRolePermissionDto(newMapping));
    }
    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable UUID id) {
        projectRolePermissionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    // create a get for getting permissions of specific project role
    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @GetMapping("/role/{roleName}")
    public List<ProjectRolePermissionDto> getPermissionsByRole(@PathVariable String roleName, @RequestParam String projectName) {
        Project project = projectRepository.findByName(projectName)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ProjectRole role = project.getProjectRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role not found in project"));
        return projectRolePermissionRepository.findByProjectAndRole(project, role)
                .stream()
                .map(ProjectRolePermissionDto::new)
                .toList();
    }
}
