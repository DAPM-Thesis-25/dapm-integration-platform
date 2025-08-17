package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectRole;
import com.dapm.security_service.models.dtos.*;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.ProjectRepository;
import com.dapm.security_service.repositories.ProjectsRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.dapm.security_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectsRolesRepository projectsRolesRepository;

    @PreAuthorize("hasAuthority('READ_PROJECT')")
    @GetMapping
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectDto::new)
                .toList();
    }
    @PreAuthorize("hasAuthority('READ_PROJECT')")
    @GetMapping("/{name}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable String name) {
        return projectRepository.findByName(name)
                .map(project -> ResponseEntity.ok(new ProjectDto(project)))
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody CreateProjectDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Project project = new Project();
        project.setId( UUID.randomUUID());
        project.setName(request.getName());
        Organization organization = userDetails.getUser().getOrganization();
        System.out.println("mra7ib");

        project.setOrganization(organization);

        Project created =projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(created));
    }

    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @PutMapping("/{name}/assign-role")
    public ResponseEntity<ProjectDto> assignRoleToProject(@PathVariable String name, @RequestBody ProjectRolesAssignmentDto projectRolesAssignmentDto) {
        Project project= projectRepository.findByName(name).orElse(null);
        ProjectRole projectRole=projectsRolesRepository.findByName(projectRolesAssignmentDto.getRole());

        project.getProjectRoles().add(projectRole);

        Project updated =projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(updated));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT')")
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteProject(@PathVariable String name) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        projectRepository.delete(project);
        return ResponseEntity.noContent().build();
    }
    //update a project with createProjectDto
    @PutMapping("/{name}/update")
    @PreAuthorize("hasAuthority('UPDATE_PROJECT:' + #name)")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable String name,
            @RequestBody CreateProjectDto request
    ) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        project.setName(request.getName());
        Project updated = projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(updated));
    }




}
