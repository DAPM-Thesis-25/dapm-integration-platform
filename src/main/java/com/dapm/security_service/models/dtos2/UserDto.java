package com.dapm.security_service.models.dtos2;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDto {
    private String username;
    private String email;
    private String orgRoleName;

    public UserDto (com.dapm.security_service.models.User user){
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.orgRoleName = user.getOrgRole().getName();

    }
}
