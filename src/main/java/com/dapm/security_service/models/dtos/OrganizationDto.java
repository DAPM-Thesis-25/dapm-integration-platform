package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class OrganizationDto {
    private UUID id;

    private String name;
    public OrganizationDto(Organization org){
        this.id=org.getId();
        this.name=org.getName();
    }
}
