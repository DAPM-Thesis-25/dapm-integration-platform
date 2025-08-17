package com.dapm.security_service.models.dtos;

import com.dapm.security_service.models.*;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CreatePipelineDto {
    private UUID id;
    private String name;
    private String ownerOrganization;
    private String description;
//    private String pipelineRole;
    private Set<UUID> processingElements = new HashSet<>();
    private List<Channel> channels = new ArrayList<>();

//    private Set<UUID> tokens = new HashSet<>();
//    private UUID createdBy;
//    private Instant createdAt;
//    private Instant updatedAt;

    public CreatePipelineDto(Pipeline pipeline){
        this.id=pipeline.getId();
        this.name=pipeline.getName();
        this.ownerOrganization=pipeline.getOwnerOrganization().getName();
        this.description=pipeline.getDescription();
        this.channels = pipeline.getChannels() != null
                ? new ArrayList<>(pipeline.getChannels())
                : new ArrayList<>();

//        this.pipelineRole=pipeline.getPipelineRole().getName();

        // Map processing elements instead of nodes
        this.processingElements = pipeline.getProcessingElements() != null
                ? pipeline.getProcessingElements().stream().map(ProcessingElement::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        // Convert tokens collection to a set of IDs
//        this.tokens = pipeline.getTokens() != null
//                ? pipeline.getTokens().stream().map(Token::getId).collect(Collectors.toSet())
//                : Collections.emptySet();

//        this.createdBy = pipeline.getCreatedBy();
//        this.createdAt = pipeline.getCreatedAt();
//        this.updatedAt = pipeline.getUpdatedAt();

    }
}
