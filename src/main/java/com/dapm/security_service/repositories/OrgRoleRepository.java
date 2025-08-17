package com.dapm.security_service.repositories;

import com.dapm.security_service.models.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgRoleRepository extends JpaRepository<OrgRole, UUID> {
    OrgRole findByName(String name);
}
