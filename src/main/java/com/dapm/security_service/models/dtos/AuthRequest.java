package com.dapm.security_service.models.dtos;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}