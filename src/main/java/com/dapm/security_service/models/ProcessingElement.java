package com.dapm.security_service.models;

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



    // The identifier of the template used for this processing element.
    @Column(name = "template_id", nullable = false)
    private String templateId;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processing_element_inputs", joinColumns = @JoinColumn(name = "processing_element_id"))
    @Column(name = "input")
    @Builder.Default
    private Set<String> inputs = new HashSet<>();


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processing_element_outputs", joinColumns = @JoinColumn(name = "processing_element_id"))
    @Column(name = "output")
    @Builder.Default
    private Set<String> outputs = new HashSet<>();

    // Visibility of this template.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processing_element_visibility", joinColumns = @JoinColumn(name = "processing_element_id"))
    @Column(name = "visible_org")
    @Builder.Default
    private Set<String> visibility = new HashSet<>();

    public void validateOwner() {
        if ((ownerOrganization == null && ownerPartnerOrganization == null) ||
                (ownerOrganization != null && ownerPartnerOrganization != null)) {
            throw new IllegalArgumentException("Exactly one owner must be set: either ownerOrganization or ownerPartnerOrganization.");
        }
    }

}
