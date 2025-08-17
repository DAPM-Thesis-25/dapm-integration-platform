package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
//    Project findByName(String name);
Optional<Project> findByName(String name);

}
