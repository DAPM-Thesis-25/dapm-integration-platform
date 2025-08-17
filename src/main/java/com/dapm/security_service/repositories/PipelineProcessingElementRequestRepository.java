package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PipelineProcessingElementRequestRepository extends JpaRepository<PipelineProcessingElementRequest, UUID> {
    boolean existsByPipelineIdAndPipelineNode_IdAndStatus(UUID pipelineId, UUID pipelineNodeId, AccessRequestStatus status);
}
