package com.dapm.security_service.repositories;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.Project;
import com.dapm.security_service.models.enums.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessingElementRepository extends JpaRepository<ProcessingElement, UUID> {


    // For POST: lookup specific PE by its ID
    Optional<ProcessingElement> findById(UUID id);

     // get all PEs except with tier PRIVATE
    List<ProcessingElement> findByTierNot(Tier tier);


    // Find by template ID
    Optional<ProcessingElement> findByTemplateId(String templateId);
}
