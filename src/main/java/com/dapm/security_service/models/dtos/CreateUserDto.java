package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CreateUserDto {

    private String username;
    private String email;
    private String passwordHash;

    private String orgRole="ADMIN_ORG_ROLE";


    public CreateUserDto(User u){
        this.username=u.getUsername();
        this.passwordHash=u.getPasswordHash();
        this.email=u.getEmail();

        this.orgRole=u.getOrgRole().getName();


    }
}
