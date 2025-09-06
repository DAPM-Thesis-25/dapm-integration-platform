package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.Pipeline;
import com.dapm.security_service.models.dtos.ConfigureValidationDto;
import com.dapm.security_service.models.dtos.MissingPermissionsDto;
import com.dapm.security_service.models.enums.AccessRequestStatus;
import com.dapm.security_service.repositories.PipelinePeInstanceRepo;
import com.dapm.security_service.repositories.PipelineProcessingElementRequestRepository;
import com.dapm.security_service.repositories.PipelineRepositoryy;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipeline/configuration")

public class PipelineConfigurationController {
    @Autowired
    private PipelineProcessingElementRequestRepository pipelinePeReqRepo;
    @Autowired
    private PipelineRepositoryy pipelineRepo;
    @Autowired
    private ProcessingElementRepository peRepo;
    @Autowired
    private PipelinePeInstanceRepo pipelinePeInstanceRepo;

    @GetMapping("/{pipelineName}/configuration-status")
    public ResponseEntity<ConfigureValidationDto> checkConfiguration(@PathVariable String pipelineName) {
        ConfigureValidationDto configStatus = getConfiguration(pipelineName);

        return configStatus != null
                ? ResponseEntity.ok(configStatus)
                : ResponseEntity.notFound().build();
    }

    private ConfigureValidationDto getConfiguration(String pipelineName) {
        Pipeline pipeline = pipelineRepo.findByName(pipelineName).
                orElseThrow(() -> new IllegalArgumentException("Pipeline Not Found"));

        // Collect partner-owned processing elements with their org names
        var partnerElements = pipeline.getProcessingElements().stream()
                .filter(pe -> pe.getOwnerPartnerOrganization() != null)
                .map(pe -> new MissingPermissionsDto(
                        pe.getTemplateId(),
                        pe.getOwnerPartnerOrganization().getName()))
                .toList();

        ConfigureValidationDto validationDto = new ConfigureValidationDto();
        if (partnerElements.isEmpty()) {
            validationDto.setStatus("VALID");
            validationDto.setMissingPermissions(List.of());
        } else {
            // Load actual partner-owned elements
            var partnerElementsEntities = pipeline.getProcessingElements().stream()
                    .filter(pe -> pe.getOwnerPartnerOrganization() != null)
                    .collect(Collectors.toList());

            // Collect missing elements: those with no PENDING request
            List<MissingPermissionsDto> missingPermissions = partnerElementsEntities.stream()
                    .filter(pe -> !pipelinePeReqRepo
                            .existsByPipelineNameAndPipelineProcessingElementInstanceAndStatus(
                                    pipeline.getName(),          // matches column pipeline_id
                                    pipelinePeInstanceRepo.findByPipelineAndProcessingElement(pipeline, pe)
                                            .orElseThrow(() -> new IllegalStateException("Pipeline PE Instance not found")),      // match the specific instance
                                    AccessRequestStatus.APPROVED))
                    .map(pe -> new MissingPermissionsDto(
                            pe.getTemplateId(),
                            pe.getOwnerPartnerOrganization().getName()))
                    .collect(Collectors.toList());

            if (missingPermissions.isEmpty()) {
                validationDto.setStatus("VALID");
                validationDto.setMissingPermissions(List.of());
            } else {
                validationDto.setStatus("INVALID");
                validationDto.setMissingPermissions(missingPermissions);
            }
        }

        return validationDto;


    }

}
