package com.dapm.security_service.controllers.PeerApi;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/peer")
public class VisibleProcessingElementController {

    @Autowired
    private ProcessingElementRepository processingElementRepository;

    @GetMapping("/visible-pes")
    public List<ProcessingElementDto> getVisiblePEs(@RequestParam String requestingOrg) {
        List<ProcessingElement> visiblePEs = processingElementRepository
                .findByOwnerOrganization_NameOrVisibilityContaining(requestingOrg, requestingOrg);

        return visiblePEs.stream()
                .map(ProcessingElementDto::new)
                .collect(Collectors.toList());
    }
}
