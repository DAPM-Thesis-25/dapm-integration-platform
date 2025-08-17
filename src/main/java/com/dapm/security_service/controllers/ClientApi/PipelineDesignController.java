package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.GetPiplineDto;
import com.dapm.security_service.models.dtos.PipelineDesignDto;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.services.PipelineDesignService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipeline/design")
public class PipelineDesignController {

    private final PipelineDesignService pipelineDesignService;

    @Value("${dapm.defaultOrgName}")
    private String orgId;

    public PipelineDesignController(PipelineDesignService pipelineDesignService) {
        this.pipelineDesignService = pipelineDesignService;
    }

    /**
     * GET endpoint to retrieve all available processing element templates for the given organization.
     * The service aggregates both local and externally visible templates.

     */
    @GetMapping("/available-pe-templates")
    public ResponseEntity<List<ProcessingElementDto>> getAvailablePeTemplates() {
        List<ProcessingElementDto> templates = pipelineDesignService.getAvailablePeTemplates(orgId);
        return ResponseEntity.ok(templates);
    }

    /**
     * POST endpoint to submit a new pipeline design.
     * Accepts a PipelineDesignDto that contains:
     * - Pipeline metadata (name, description)
     * - A list of processing elements (each with temporary IDs, templateId, ownerOrganization, inputs, and outputs)
     * - Channels defined as lists of processing element IDs (the temporary IDs will be replaced with persistent IDs)
     *
     * The service maps this DTO to a Pipeline entity and persists it.
     */
    @PreAuthorize(" hasAuthority('CREATE_PIPELINE:' + #pipelineDesignDto.getProject())")
    @PostMapping
    public ResponseEntity<GetPiplineDto> createPipelineDesign(@RequestBody PipelineDesignDto pipelineDesignDto) {
        Pipeline createdPipeline = pipelineDesignService.savePipelineDesign(pipelineDesignDto);
        // Convert the created Pipeline entity back to DTO for response
        GetPiplineDto createdPipelineDto = new GetPiplineDto(createdPipeline);
        // Return the created pipeline design as a response
        return ResponseEntity.ok(createdPipelineDto);
    }
}
