package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.ProjectPermission;
import com.dapm.security_service.models.enums.ProjectPermAction;
import com.dapm.security_service.models.enums.ProjectScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPermissionDto {
 private UUID id;
 private ProjectPermAction action;


 public ProjectPermissionDto(ProjectPermission permission){
        this.id = permission.getId();
        this.action = permission.getAction();

 }



}
