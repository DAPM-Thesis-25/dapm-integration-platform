package com.dapm.security_service.repositories;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessingElementRepository extends JpaRepository<ProcessingElement, UUID> {

    // For GET: show all PEs owned by or visible to the org
    List<ProcessingElement> findByOwnerOrganization_NameOrVisibilityContaining(String ownerOrg, String visibleToOrg);

    // For POST: lookup specific PE by its ID
    Optional<ProcessingElement> findById(UUID id);
    List<ProcessingElement> findByVisibilityContaining(String org);


    // Find by template ID
    Optional<ProcessingElement> findByTemplateId(String templateId);
}
