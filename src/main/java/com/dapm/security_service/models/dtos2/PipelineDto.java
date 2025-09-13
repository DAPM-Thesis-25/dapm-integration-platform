package com.dapm.security_service.models.dtos2;

import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.enums.PipelinePhase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelineDto {
    private String pipelineName;
    private String ProjectName;
    private PipelinePhase pipelinePhase;
    private List<String> processingElementIds;

    public PipelineDto (Pipeline pipeline){
        this.pipelineName = pipeline.getName();
        this.ProjectName = pipeline.getProject().getName();
        this.pipelinePhase = pipeline.getPipelinePhase();
        if (pipeline.getProcessingElements() != null) {
            this.processingElementIds = pipeline.getProcessingElements().stream()
                    .map(ProcessingElement::getTemplateId) // extract templateId
                    .toList(); // Java 16+, or use Collectors.toList() in earlier versions
        }
    }
}

