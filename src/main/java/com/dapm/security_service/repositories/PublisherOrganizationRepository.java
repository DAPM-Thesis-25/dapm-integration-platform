package com.dapm.security_service.repositories;


import com.dapm.security_service.models.PublisherOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PublisherOrganizationRepository extends JpaRepository<PublisherOrganization, UUID> {
    Optional<PublisherOrganization> findByName(String name);
}
