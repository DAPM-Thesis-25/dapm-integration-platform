package com.dapm.security_service.repositories;


import com.dapm.security_service.models.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectsRolesRepository extends JpaRepository<ProjectRole, UUID> {
    ProjectRole findByName(String name);
}
