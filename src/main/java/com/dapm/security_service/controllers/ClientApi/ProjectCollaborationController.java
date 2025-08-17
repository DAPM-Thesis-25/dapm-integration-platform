package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.ProjectCollaboration;

import com.dapm.security_service.repositories.ProjectCollaborationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-collaborations")
public class ProjectCollaborationController {

    @Autowired
    private ProjectCollaborationRepository projectCollaborationRepository;

    @GetMapping
    public List<ProjectCollaborationDto> getAllCollabs() {
        return projectCollaborationRepository.findAll().stream()
                .map(ProjectCollaborationDto::fromEntity)
                .toList();
    }

    // DTO definition
    public static class ProjectCollaborationDto {
        public UUID id;
        public String organizationName;
        public String partnerOrganizationName;
        public String projectName;

        public ProjectCollaborationDto(UUID id, String organizationName, String partnerOrganizationName, String projectName) {
            this.id = id;
            this.organizationName = organizationName;
            this.partnerOrganizationName = partnerOrganizationName;
            this.projectName = projectName;
        }

        public static ProjectCollaborationDto fromEntity(ProjectCollaboration pc) {
            return new ProjectCollaborationDto(
                    pc.getId(),
                    pc.getOrganization().getName(),
                    pc.getPartnerOrganization() != null ? pc.getPartnerOrganization().getName() : null,
                    pc.getProject().getName()
            );
        }
    }
}

