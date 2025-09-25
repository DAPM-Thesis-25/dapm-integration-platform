package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.Organization;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectRole;
import com.dapm.security_service.models.User;
import com.dapm.security_service.models.dtos.*;
import com.dapm.security_service.repositories.OrganizationRepository;
import com.dapm.security_service.repositories.ProjectRepository;
import com.dapm.security_service.repositories.ProjectsRolesRepository;
import com.dapm.security_service.repositories.UserRoleAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.dapm.security_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dapm.security_service.models.UserRoleAssignment;

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

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;
    @Autowired
    private ProjectRepository projectsRepository;

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
        project.setOrganization(organization);

        for (String roleName : request.getRoles()) {
            ProjectRole projectRole=projectsRolesRepository.findByName(roleName);
            if (projectRole != null) {
                project.getProjectRoles().add(projectRole);
            }
        }

        Project created =projectRepository.save(project);
        return ResponseEntity.ok(new ProjectDto(created));
    }

    @PreAuthorize("hasAuthority('ASSIGN_PROJECT_ROLES')")
    @PutMapping("/{name}/assign-role")
    public ResponseEntity<ProjectDto> assignRoleToProject(@PathVariable String name, @RequestBody ProjectRolesAssignmentDto projectRolesAssignmentDto) {
        Project project= projectRepository.findByName(name).orElse(null);

        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        // iterate over roles in dto and add them to project
        for (String roleName : projectRolesAssignmentDto.getRoles()) {
            ProjectRole projectRole=projectsRolesRepository.findByName(roleName);
            if (projectRole != null) {
                project.getProjectRoles().add(projectRole);
            }
        }
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
    @Transactional(readOnly = true)
    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectDto>> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();

        List<ProjectDto> myProjects = userRoleAssignmentRepository.findByUser(user).stream()
                .map(UserRoleAssignment::getProject) // get project from assignment
                .map(ProjectDto::new)                // map to DTO
                .toList();

        return ResponseEntity.ok(myProjects);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<ProjectRole>> getAllProjectRoles() {
        List<ProjectRole> roles = projectsRolesRepository.findAll().stream()
                .toList();
        return ResponseEntity.ok(roles);
    }

    // add a new role to projectsRolesRepository
    @PostMapping("/roles/add")
    public ResponseEntity<ProjectRole> addProjectRole(@RequestBody ProjectRole projectRole) {

        ProjectRole existingRole = projectsRolesRepository.findByName(projectRole.getName());
        if (existingRole != null) {
            return ResponseEntity.status(409).body(null); // Conflict
        }
        projectRole.setId(UUID.randomUUID());
        ProjectRole created = projectsRolesRepository.save(projectRole);
        return ResponseEntity.ok(created);
    }




}
