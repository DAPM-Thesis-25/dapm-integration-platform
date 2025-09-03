package com.dapm.security_service.services;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.dtos.PipelineDesignDto;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.models.enums.Tier;
import com.dapm.security_service.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PipelineDesignService {

    private final PipelineRepositoryy pipelineRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProcessingElementRepository processingElementRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCollaborationRepository projectCollaborationRepository;
    private final ObjectMapper objectMapper;
    private final VisiblePeClient visiblePeClient;

    @Autowired
    private PublisherOrganizationRepository publisherOrganizationRepository;

    @Value("${dapm.defaultOrgName}")
    private String orgName;

    public PipelineDesignService(
            PipelineRepositoryy pipelineRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            ProcessingElementRepository processingElementRepository,
            ObjectMapper objectMapper,
            ProjectRepository projectRepository,
            ProjectCollaborationRepository projectCollaborationRepository,
            VisiblePeClient visiblePeClient
    ) {
        this.pipelineRepository = pipelineRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.processingElementRepository = processingElementRepository;
        this.objectMapper = objectMapper;
        this.visiblePeClient = visiblePeClient;
        this.projectCollaborationRepository = projectCollaborationRepository;
        this.projectRepository = projectRepository;
    }

//    public List<ProcessingElementDto> getAvailablePeTemplates(String org) {
//        List<ProcessingElementDto> localDtos = processingElementRepository
//                .findByOwnerOrganization_NameOrVisibilityContaining(org, org)
//                .stream()
//                .map(ProcessingElementDto::new)
//                .collect(Collectors.toList());
//
//        List<ProcessingElementDto> remoteDtos = visiblePeClient.getVisiblePEsFromOrgB(org);
//        // now it is hardcoded to OrgB, but you can make it dynamic if needed
//        publisherOrganizationRepository.findByName("OrgB")
//                .orElseGet(() -> publisherOrganizationRepository.save(new PublisherOrganization(UUID.randomUUID(),"OrgB", Tier.FREE)));
//
//        Set<UUID> existingIds = localDtos.stream()
//                .map(ProcessingElementDto::getId)
//                .collect(Collectors.toSet());
//
//        remoteDtos.stream()
//                .filter(dto -> dto.getId() != null && !existingIds.contains(dto.getId()))
//                .forEach(localDtos::add);
//
//        return localDtos;
//    }

    public Pipeline savePipelineDesign(PipelineDesignDto dto) {
        Map<String, UUID> idMap = new HashMap<>();
        Set<ProcessingElement> elements = new HashSet<>();

        if (dto.getProcessingElements() != null) {
            for (ProcessingElementDto peDto : dto.getProcessingElements()) {
                String tempId = UUID.randomUUID().toString();
                UUID newId = UUID.randomUUID();
                idMap.put(tempId, newId);

                ProcessingElement.ProcessingElementBuilder builder = ProcessingElement.builder()
                        .id(newId)
                        .templateId(peDto.getTemplateId());
//                        .inputs(peDto.getInputs())
//                        .outputs(peDto.getOutputs());

                if (peDto.getOwnerOrganization().equals(orgName)) {
                    builder.ownerOrganization(getOrganization(peDto.getOwnerOrganization()));
                } else {
                    builder.ownerPartnerOrganization(getPartnerOrganization(peDto.getOwnerOrganization()));
                    // add to the project collaboration repository
                    ProjectCollaboration collaboration = new ProjectCollaboration();
                    collaboration.setId(UUID.randomUUID());
                    collaboration.setProject(projectRepository.findByName(dto.getProject())
                            .orElseThrow(() -> new RuntimeException("Project '" + dto.getProject() + "' not found")));
                    collaboration.setPartnerOrganization(getPartnerOrganization(peDto.getOwnerOrganization()));
                    collaboration.setOrganization(getOrganization(orgName));
                    projectCollaborationRepository.save(collaboration);
                }

                ProcessingElement pe = builder.build();
                pe.validateOwner(); // Optional: add this to ensure XOR logic
                elements.add(pe);
            }

        }

        List<Channel> updatedChannels = dto.getChannels().stream()
                .map(pair -> new Channel(
                        idMap.get(pair.get(0)), // convert temp source
                        idMap.get(pair.get(1))  // convert temp target
                ))
                .collect(Collectors.toList());



        // Ensure the project exists or response error
        Project project = projectRepository.findByName(dto.getProject())
                .orElseThrow(() -> new RuntimeException("Project '" + dto.getProject() + "' not found"));

        Pipeline pipeline = Pipeline.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .project(project)
                .ownerOrganization(getDefaultOwnerOrganization())
                .pipelineRole(null)
                .processingElements(elements)
                .channels(updatedChannels)
                .createdBy(getAuthenticatedUserId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return pipelineRepository.save(pipeline);
    }

    private Organization getOrganization(String name) {
        return organizationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Organization '" + name + "' not found"));
    }
    private PublisherOrganization getPartnerOrganization(String name) {
        return publisherOrganizationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Partner Organization '" + name + "' not found"));
    }

    private Organization getDefaultOwnerOrganization() {
        return getOrganization("OrgA");
    }

    private UUID getAuthenticatedUserId() {
        return userRepository.findByUsername("alice")
                .orElseThrow(() -> new RuntimeException("Default user 'alice' not found"))
                .getId();
    }
}
