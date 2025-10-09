package com.dapm.security_service.services;

import com.dapm.security_service.config.JwtService;
import com.dapm.security_service.exceptions.DuplicateFieldException;
import com.dapm.security_service.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.AuthRequest;
import com.dapm.security_service.models.dtos.AuthResponse;
import com.dapm.security_service.models.dtos.ChangePasswordDto;
import com.dapm.security_service.models.dtos.CreateUserDto;
import com.dapm.security_service.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService2 {

    @Autowired private UserDetailsRepository repository;
    @Autowired private OrganizationRepository organizationRepository;

    @Autowired private OrgRoleRepository orgRoleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public void register(CreateUserDto user, CustomUserDetails userDetails) {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail(user.getEmail());
        newUser.setUsername(user.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        Organization organization = userDetails.getUser().getOrganization();
        newUser.setOrganization(organization);

        OrgRole orgRole = orgRoleRepository.findByName(user.getOrgRole());
        if (orgRole == null) {
            throw new IllegalArgumentException("OrgRole not found");
        }

        newUser.setOrgRole(orgRole);

        try {
            repository.save(newUser);
        } catch (DataIntegrityViolationException ex) {
            // Postgres unique constraint violation
            if (ex.getMessage().contains("email")) {
                throw new DuplicateFieldException("email", "Email already exists");
            } else if (ex.getMessage().contains("username")) {
                throw new DuplicateFieldException("username", "Username already exists");
            }
            throw ex; // rethrow for unexpected errors
        }
    }


    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public void changePassword(ChangePasswordDto request) {
        // Get current logged-in user from SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if old password matches
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Encode new password and update
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }
}
