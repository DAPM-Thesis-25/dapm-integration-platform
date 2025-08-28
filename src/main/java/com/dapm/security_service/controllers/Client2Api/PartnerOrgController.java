package com.dapm.security_service.controllers.Client2Api;
import com.dapm.security_service.models.PublisherOrganization;
import com.dapm.security_service.models.SubscriberOrganization;
import com.dapm.security_service.repositories.PublisherOrganizationRepository;
import com.dapm.security_service.repositories.SubscriberOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partner-organizations")
public class PartnerOrgController {
    @Autowired
    private SubscriberOrganizationRepository subscriberOrganizationRepository;

    @Autowired
    private PublisherOrganizationRepository publisherOrganizationRepository;

    @GetMapping("/subscribers")
    public List<SubscriberOrganization> getAllSubscriberOrganizations() {
        return subscriberOrganizationRepository.findAll();
    }

    @GetMapping("/publishers")
    public List<PublisherOrganization> getAllPublisherOrganizations() {
        return publisherOrganizationRepository.findAll();
    }

}
