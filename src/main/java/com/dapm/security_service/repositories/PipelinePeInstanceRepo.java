package com.dapm.security_service.repositories;

import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.PipelineProcessingElementInstance;
import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.ProcessingElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PipelinePeInstanceRepo extends JpaRepository<PipelineProcessingElementInstance, UUID> {
    Optional<PipelineProcessingElementInstance> findByPipelineAndProcessingElement(
            Pipeline pipeline,
            ProcessingElement processingElement);

}
