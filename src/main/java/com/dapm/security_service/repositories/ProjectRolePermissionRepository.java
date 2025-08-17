package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.ProjectPermission;
import com.dapm.security_service.models.ProjectRole;
import com.dapm.security_service.models.ProjectRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRolePermissionRepository extends JpaRepository<ProjectRolePermission, UUID> {
    Optional<ProjectRolePermission> findByProjectAndPermissionAndRole(Project project, ProjectPermission permission, ProjectRole role);
    List<ProjectRolePermission> findByProjectId(UUID projectId);
    List<ProjectRolePermission> findByProjectAndRole(Project project, ProjectRole role);
    @Query("""
    SELECT prp FROM ProjectRolePermission prp
    JOIN FETCH prp.permission
    JOIN FETCH prp.project
    JOIN FETCH prp.role
    WHERE prp.project = :project AND prp.role = :role
""")
    List<ProjectRolePermission> findByProjectAndRoleWithAll(
            @Param("project") Project project,
            @Param("role") ProjectRole role
    );

}
