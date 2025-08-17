package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.*;
import lombok.Data;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class PipelineDto {
    private UUID id;
    private String name;

    private UUID ownerOrganizationId;
    private String ownerOrganizationName;

    private String description;

    private List<Channel> channels;

    private String project;


    //    private UUID pipelineRoleId;
    private String pipelineRoleName;

    // Changed from nodeIds to processingElementIds
    private Set<UUID> processingElementIds;
//    private Set<UUID> tokenIds;

    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;


    public PipelineDto() {}

    public PipelineDto(Pipeline pipeline) {
        this.id = pipeline.getId();
        this.name = pipeline.getName();

        Organization ownerOrg = pipeline.getOwnerOrganization();
        if (ownerOrg != null) {
            this.ownerOrganizationId = ownerOrg.getId();
            this.ownerOrganizationName = ownerOrg.getName();
        }

        this.description = pipeline.getDescription();

//        Role role = pipeline.getPipelineRole();
//        if (role != null) {
//            this.pipelineRoleId = role.getId();
//            this.pipelineRoleName = role.getName();
//        }

        // Map processing elements instead of nodes
        this.processingElementIds = pipeline.getProcessingElements() != null
                ? pipeline.getProcessingElements().stream().map(ProcessingElement::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        // Convert tokens collection to a set of IDs
//        this.tokenIds = pipeline.getTokens() != null
//                ? pipeline.getTokens().stream().map(Token::getId).collect(Collectors.toSet())
//                : Collections.emptySet();
        this.project= pipeline.getProject().getName();

        this.createdBy = pipeline.getCreatedBy();
        this.createdAt = pipeline.getCreatedAt();
        this.updatedAt = pipeline.getUpdatedAt();
        this.channels=pipeline.getChannels();
    }

}
