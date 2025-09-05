package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.dtos.ConfigureValidationDto;
import com.dapm.security_service.models.dtos.MissingPermissionsDto;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.repositories.PipelineRepositoryy;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/configure-pipeline")
public class ConfigurePipelineController {
    @Autowired
    private PipelineRepositoryy pipelineRepository;

    @Autowired
    private PipelineProcessingElementRequestRepository pipelineProcessingElementRequestRepository;
//    @PreAuthorize("@pipelineAccessEvaluator.hasPermission(#pipelineName, authentication, 'CONFIGURE_PIPELINE')")
//    @GetMapping("/{pipelineName}/validate")
//    public ConfigureValidationDto validatePipeline(@PathVariable String pipelineName) {
//        var pipeline = pipelineRepository.findByName(pipelineName)
//                .orElseThrow(() -> new RuntimeException("Pipeline '" + pipelineName + "' not found"));
//
//        // Collect partner-owned processing elements with their org names
//        var partnerElements = pipeline.getProcessingElements().stream()
//                .filter(pe -> pe.getOwnerPartnerOrganization() != null)
//                .map(pe -> new MissingPermissionsDto(
//                        pe.getTemplateId(),
//                        pe.getOwnerPartnerOrganization().getName()))
//                .toList();
//
//
//        ConfigureValidationDto validationDto = new ConfigureValidationDto();
//        if (partnerElements.isEmpty()) {
//            validationDto.setStatus("VALID");
//            validationDto.setMissingPermissions(List.of());
//        } else {
//            // Load actual partner-owned elements
//            var partnerElementsEntities = pipeline.getProcessingElements().stream()
//                    .filter(pe -> pe.getOwnerPartnerOrganization() != null)
//                    .collect(Collectors.toList());
//
//            // Collect missing elements: those with no PENDING request
//            List<MissingPermissionsDto> missingPermissions = partnerElementsEntities.stream()
//                    .filter(pe -> !pipelineProcessingElementRequestRepository
//                            .existsByPipelineIdAndPipelineNode_IdAndStatus(
//                                    pipeline.getId(), pe.getId(), AccessRequestStatus.APPROVED))
//                    .map(pe -> new MissingPermissionsDto(
//                            pe.getTemplateId(),
//                            pe.getOwnerPartnerOrganization().getName()))
//                    .collect(Collectors.toList());
//
//            if (missingPermissions.isEmpty()) {
//                validationDto.setStatus("VALID");
//                validationDto.setMissingPermissions(List.of());
//            } else {
//                validationDto.setStatus("INVALID");
//                validationDto.setMissingPermissions(missingPermissions);
//            }
//        }
//
//
//        return validationDto;
//    }
}
