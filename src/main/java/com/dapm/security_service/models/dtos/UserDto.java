package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.User;
import com.dapm.security_service.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String organization;
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.organization = (user.getOrganization() != null) ? user.getOrganization().getName() : null;

    }
}
