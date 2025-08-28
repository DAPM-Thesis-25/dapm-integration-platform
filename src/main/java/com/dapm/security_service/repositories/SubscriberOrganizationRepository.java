package com.dapm.security_service.repositories;

import com.dapm.security_service.models.SubscriberOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriberOrganizationRepository extends JpaRepository<SubscriberOrganization, UUID> {
    Optional<SubscriberOrganization> findByName(String name);
}
