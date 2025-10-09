package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.Project;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class CreateProjectDto {

    private String name;
    private Set<String> roles = new HashSet<>();


    public CreateProjectDto(Project project) {
        this.name = project.getName();

    }


}
