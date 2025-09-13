package com.dapm.security_service.models;

import com.dapm.security_service.models.enums.Tier;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Entity
@Table(name = "processing_element")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingElement {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // The organization that owns this processing element.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_organization_id")
    private Organization ownerOrganization;

    // The organization that owns this processing element.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_partner_organization_id")
    private PublisherOrganization ownerPartnerOrganization;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private Tier tier;


    // The identifier of the template used for this processing element.
    @Column(name = "template_id", nullable = false, unique = true)
    private String templateId;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "instance_number", nullable = false)
    private Integer instanceNumber;

    @Column(name = "name", nullable = false, unique = false)
    private String hostURL;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processing_element_inputs", joinColumns = @JoinColumn(name = "processing_element_id"))
    @Column(name = "input")
    @Builder.Default
    private Set<String> inputs = new HashSet<>();


    @Column(name = "output", nullable = true)
    private String output;


    public void validateOwner() {
        if ((ownerOrganization == null && ownerPartnerOrganization == null) ||
                (ownerOrganization != null && ownerPartnerOrganization != null)) {
            throw new IllegalArgumentException("Exactly one owner must be set: either ownerOrganization or ownerPartnerOrganization.");
        }
    }

}
