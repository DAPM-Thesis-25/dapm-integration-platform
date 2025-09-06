package com.dapm.security_service.repositories;

import com.dapm.security_service.models.PipelineProcessingElementInstance;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PipelineProcessingElementRequestRepository extends JpaRepository<PipelineProcessingElementRequest, UUID> {
//    boolean existsByPipelineIdAndPipelineNode_IdAndStatus(UUID pipelineId, UUID pipelineNodeId, AccessRequestStatus status);
//boolean existsByPipelineNameAndProcessingElement_TemplateIdAndInstanceNumberAndStatus(
//        String pipelineName,
//        String templateId,
//        Integer instanceNumber,
//        AccessRequestStatus status
//);

    boolean existsByPipelineNameAndPipelineProcessingElementInstanceAndStatus(
            String pipelineName,
            PipelineProcessingElementInstance instance,
            AccessRequestStatus status
    );

}
