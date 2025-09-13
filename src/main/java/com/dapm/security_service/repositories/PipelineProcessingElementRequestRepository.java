package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PipelineProcessingElementInstance;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PipelineProcessingElementRequestRepository extends JpaRepository<PipelineProcessingElementRequest, UUID> {
    boolean existsByPipelineNameAndPipelineProcessingElementInstanceAndStatus(
            String pipelineName,
            PipelineProcessingElementInstance instance,
            AccessRequestStatus status
    );

    List<PipelineProcessingElementRequest> findByRequesterInfo_Organization(String organization);

    List<PipelineProcessingElementRequest> findByRequesterInfo_OrganizationNot(String organization);

}
