package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.*;
import com.dapm.security_service.models.dtos2.CustomizedRoleAndPermissionsDto;
import com.dapm.security_service.models.enums.ProjectPermAction;
import com.dapm.security_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.dapm.security_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Set;
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
    @Autowired
    private  ProjPermissionRepository projPermissionRepository;
    @Autowired
    private ProjectRolePermissionRepository projectRolePermissionRepository;

    @PreAuthorize("hasAuthority('READ_PROJECT')")
    @GetMapping("/all")
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




        Set<ProjectPermAction> leaderActions = Set.of(
                ProjectPermAction.CONFIGURE_PIPELINE,
                ProjectPermAction.UPDATE_PROJECT,
                ProjectPermAction.ASSIGN_USER_PROJECT_ROLE,
                ProjectPermAction.READ_PES
                ,ProjectPermAction.CREATE_PIPELINE
        );
        Set<ProjectPermAction> researcherActions = Set.of(
                ProjectPermAction.CONFIGURE_PIPELINE,
                ProjectPermAction.START_PIPELINE,
                ProjectPermAction.READ_PIPELINE,
                ProjectPermAction.READ_PIPELINES,
                ProjectPermAction.CREATE_PIPELINE
        );
        ProjectRole roleLeader=projectsRolesRepository.findByName("leader");
        ProjectRole roleResearcher=projectsRolesRepository.findByName("researcher");
        project.getProjectRoles().add(roleResearcher);
        project.getProjectRoles().add(roleLeader);
        Project created =projectRepository.save(project);
        for (ProjectPermAction action : leaderActions) {
            ProjectPermission projectPermission=projPermissionRepository.findByAction(action);
            if (projectPermission == null) {
                continue;
            }
            var existing = projectRolePermissionRepository.findByProjectAndPermissionAndRole(project, projectPermission, roleLeader);
            if (existing.isPresent()) {
                continue;
            }
            var newMapping = ProjectRolePermission.builder()
                    .id(UUID.randomUUID())
                    .project(project)
                    .role(roleLeader)
                    .permission(projectPermission)
                    .build();
            newMapping = projectRolePermissionRepository.save(newMapping);
        }

        for (ProjectPermAction action : researcherActions) {
            ProjectPermission projectPermission=projPermissionRepository.findByAction(action);
            if (projectPermission == null) {
                continue;
            }
            var existing = projectRolePermissionRepository.findByProjectAndPermissionAndRole(project, projectPermission, roleResearcher);
            if (existing.isPresent()) {
                continue;
            }
            var newMapping = ProjectRolePermission.builder()
                    .id(UUID.randomUUID())
                    .project(project)
                    .role(roleResearcher)
                    .permission(projectPermission)
                    .build();
            newMapping = projectRolePermissionRepository.save(newMapping);
        }




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



    @PostMapping("assign-roles-and-permissions")
    public ResponseEntity<ProjectDto> addProjectRole(@RequestBody CustomizedRoleAndPermissionsDto projectRole) {
        Project project= projectsRepository.findByName(projectRole.getProjectName()).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        ProjectRole existingRole = projectsRolesRepository.findByName(projectRole.getRoleName());
        if (existingRole == null) {
            existingRole = new ProjectRole();
            existingRole.setId(UUID.randomUUID());
            existingRole.setName(projectRole.getRoleName());
            existingRole = projectsRolesRepository.save(existingRole);
        }
        project.getProjectRoles().add(existingRole);
        projectsRepository.save(project);

        for (ProjectPermAction action : projectRole.getPermissions()) {
            ProjectPermission projectPermission=projPermissionRepository.findByAction(action);
            if (projectPermission == null) {
                continue;
            }
            var existing = projectRolePermissionRepository.findByProjectAndPermissionAndRole(project, projectPermission, existingRole);
            if (existing.isPresent()) {
                continue;
            }
            var newMapping = ProjectRolePermission.builder()
                    .id(UUID.randomUUID())
                    .project(project)
                    .role(existingRole)
                    .permission(projectPermission)
                    .build();
            newMapping = projectRolePermissionRepository.save(newMapping);
        }

        return ResponseEntity.ok(new ProjectDto(project));




    }




}
