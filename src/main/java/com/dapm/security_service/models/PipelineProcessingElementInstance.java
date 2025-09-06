package com.dapm.security_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Entity
@Table(name = "pipeline_pe_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineProcessingElementInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_element_id", nullable = false)
    private ProcessingElement processingElement;

    @Column(name = "instance_number", nullable = false)
    private Integer instanceNumber;
}

