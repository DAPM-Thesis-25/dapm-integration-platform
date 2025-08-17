package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.dapm.security_service.models.dtos.AuthRequest;
import com.dapm.security_service.models.dtos.AuthResponse;
import com.dapm.security_service.models.dtos.ChangePasswordDto;
import com.dapm.security_service.models.dtos.CreateUserDto;
import com.dapm.security_service.services.AuthenticationService2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    public final AuthenticationService2 service;

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody CreateUserDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        service.register(request, userDetails);
        return ResponseEntity.ok("User registered successfully.");
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> register(
        @RequestBody AuthRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto request) {
        service.changePassword(request);
        return ResponseEntity.ok().body("Password changed successfully");
    }

}

