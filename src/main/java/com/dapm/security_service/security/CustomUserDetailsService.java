package com.dapm.security_service.security;

import com.dapm.security_service.models.User;
import com.dapm.security_service.repositories.OrgPermissionRepository;
import com.dapm.security_service.repositories.ProjectRolePermissionRepository;
import com.dapm.security_service.repositories.UserRepository;
import com.dapm.security_service.repositories.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final OrgPermissionRepository orgPermissionRepository;
    private final  UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final ProjectRolePermissionRepository projectRolePermissionRepository;


    // Loads user details by username for Spring Security during login.
// Fetches user's org role and resolves its permissions to authorities.

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println("yi");

        var permissions = orgPermissionRepository.findByOrgRole(user.getOrgRole());

        var authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getAction().name()))
                .collect(Collectors.toSet());

        var assignments = userRoleAssignmentRepository.findByUser(user);

        for (var assignment : assignments) {
            var project = assignment.getProject();
            var role = assignment.getRole();

            var prpList = projectRolePermissionRepository.findByProjectAndRoleWithAll(project, role);

            for (var prp : prpList) {
                String permissionAction = prp.getPermission().getAction().name(); // e.g., "PIPELINE_EDIT"
                String authority = permissionAction + ":" + prp.getProject().getName(); // ✅ Fix here
                authorities.add(new SimpleGrantedAuthority(authority));
            }

        }
        System.out.println("Authorities for user " + username + ": " + authorities);

        return new CustomUserDetails(user, authorities);
    }
}


