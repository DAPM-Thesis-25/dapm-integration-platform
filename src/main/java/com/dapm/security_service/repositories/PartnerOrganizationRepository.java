package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PartnerOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerOrganizationRepository extends JpaRepository<PartnerOrganization, UUID> {
    Optional<PartnerOrganization> findByName(String name);
}


