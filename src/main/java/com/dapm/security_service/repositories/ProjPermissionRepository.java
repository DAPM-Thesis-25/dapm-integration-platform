package com.dapm.security_service.repositories;

import com.dapm.security_service.models.OrgPermission;
import com.dapm.security_service.models.OrgRole;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectPermission;
import com.dapm.security_service.models.enums.OrgPermAction;
import com.dapm.security_service.models.enums.ProjectPermAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjPermissionRepository extends JpaRepository<ProjectPermission, UUID> {
    ProjectPermission findByAction(ProjectPermAction action);
//    List<ProjectPermission> findByOrgRole(OrgRole orgRole);


}

