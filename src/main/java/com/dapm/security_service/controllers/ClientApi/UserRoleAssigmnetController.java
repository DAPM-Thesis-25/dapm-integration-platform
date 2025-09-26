package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectRole;
import com.dapm.security_service.models.User;
import com.dapm.security_service.models.UserRoleAssignment;
import com.dapm.security_service.models.dtos.AssignUserRoleDto;
import com.dapm.security_service.models.dtos.CreateProjectDto;
import com.dapm.security_service.repositories.ProjectRepository;
import com.dapm.security_service.repositories.UserRepository;
import com.dapm.security_service.repositories.UserRoleAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-role-assignments")
public class UserRoleAssigmnetController {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  ProjectRepository projectRepository;

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    // get all assignments by project
    @GetMapping
//    @PreAuthorize("hasAuthority('READ_USER_ROLES') or hasAuthority('READ_USER_PROJECT_ROLES:' + #projectName)")
    public ResponseEntity<?> getAllAssignmentsByProject(@RequestParam String projectName) {
        Optional<Project> projectOpt = projectRepository.findByName(projectName);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Project not found");
        }
        Project project = projectOpt.get();
        var assignments = userRoleAssignmentRepository.findByProject(project)
                .stream()
                .map(UserRoleAssigmnetController::toDto)
                .toList();
        return ResponseEntity.ok(assignments);
    }



    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ASSIGN_USER_ROLE') or hasAuthority('ASSIGN_USER_PROJECT_ROLE:' + #request.getProject())")
    public ResponseEntity<?> assignUserRole(@RequestBody AssignUserRoleDto request) {

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Optional<Project> projectOpt = projectRepository.findByName(request.getProject());
        if (projectOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Project not found");
        }

        User user = userOpt.get();
        Project project = projectOpt.get();

        // Find role by name only in project's roles
        Optional<ProjectRole> roleOpt = project.getProjectRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(request.getRole()))
                .findFirst();

        if (roleOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Role not found in project");
        }

        ProjectRole role = roleOpt.get();

        Optional<UserRoleAssignment> existingAssignment = userRoleAssignmentRepository
                .findByUserAndProject(user, project);

        if (existingAssignment.isPresent()) {
            // Force init before mapping to DTO
            UserRoleAssignment ea = existingAssignment.get();
            ea.getUser().getUsername();
            ea.getProject().getName();
            ea.getRole().getName();
            return ResponseEntity.ok(toDto(ea));
        }

        UserRoleAssignment assignment = UserRoleAssignment.builder()
                .user(user)
                .project(project)
                .role(role)
                .build();

        UserRoleAssignment saved = userRoleAssignmentRepository.save(assignment);

        // 👇 Force initialization here
        saved.getUser().getUsername();
        saved.getProject().getName();
        saved.getRole().getName();

        return ResponseEntity.ok(toDto(saved));
    }



    public static AssignUserRoleDto toDto(UserRoleAssignment assignment) {
        return new AssignUserRoleDto(
                assignment.getUser().getUsername(),
                assignment.getRole().getName(),
                assignment.getProject().getName()

        );
    }



}
