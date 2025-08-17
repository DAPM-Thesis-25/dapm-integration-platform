package com.dapm.security_service.controllers.ClientApi;
import com.dapm.security_service.models.PartnerOrganization;
import com.dapm.security_service.repositories.PartnerOrganizationRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import com.dapm.security_service.models.Organization;
import com.dapm.security_service.repositories.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partner-organizations")
public class PartnerOrgController {
    @Autowired
    private PartnerOrganizationRepository partnerOrganizationRepository;

    @GetMapping
    public List<PartnerOrganization> getAllOrganizations() {
        return partnerOrganizationRepository.findAll();
    }

}
