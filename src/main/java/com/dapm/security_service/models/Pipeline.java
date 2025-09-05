package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pipeline")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pipeline {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_organization_id", nullable = false)
    private Organization ownerOrganization;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


    // Pipeline execution role (can be linked to the user later)
//    @OneToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "pipeline_role_id", nullable = true)
//    private Role pipelineRole;

    // Instead of nodes, we reference processing elements
//    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinTable(
//            name = "pipeline_processing_element_mapping",
//            joinColumns = @JoinColumn(name = "pipeline_id"),
//            inverseJoinColumns = @JoinColumn(name = "processing_element_id")
//    )
//    @Builder.Default
//    private Set<ProcessingElement> processingElements = new HashSet<>();



//    @ElementCollection
//    @CollectionTable(name = "pipeline_channels", joinColumns = @JoinColumn(name = "pipeline_id"))
//    private List<Channel> channels;


//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Builder.Default
//    private Set<Token> tokens = new HashSet<>();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

//    @Column(name = "updated_at", nullable = false)
//    private Instant updatedAt;
}
