package com.dapm.security_service.controllers.PeerApi;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.dtos2.GetPeerRequest;
import com.dapm.security_service.models.enums.Tier;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.SubscriberOrganizationRepository;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/peer")
public class PeerRequestProcessingElementConfigsController {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TokenVerificationService verificationService;
    @Autowired
    private ProcessingElementRepository peRepository;
    @Autowired
    private SubscriberOrganizationRepository partnerOrganizationRepository;
    @Value("${dapm.defaultOrgName}")
    private String orgName;

    @PostMapping("/availablePeConfigs")
    public ResponseEntity<?> requestPeConfigsJson(@RequestBody GetPeerRequest request) throws Exception {
        System.out.println("I received a request for PE configs JSON");

        // 1) Verify org from token
        String callerOrg = verificationService.verifyTokenAndGetOrganization(request.getToken());
        if (callerOrg == null || callerOrg.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid or missing token");
        }
        System.out.println("Caller organization: " + callerOrg);
        partnerOrganizationRepository.findByName(callerOrg)
                .orElseThrow(() -> new IllegalArgumentException("Partner Organization not found or handshake not completed."));

        // 2) Visible PEs
        List<ProcessingElement> visiblePEs = peRepository.findByTierNot(Tier.PRIVATE);

        // 3) Schema root
        Path root = Path.of("/runtime-configs").toAbsolutePath().normalize();

        // 4) Build JSON response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("organization", callerOrg);
        List<Map<String, Object>> items = new ArrayList<>();

        for (ProcessingElement pe : visiblePEs) {
            String expectedFile = orgName.toLowerCase() + "_" +
                    pe.getTemplateId().toLowerCase() + "_config_schema.json";
            Path schemaPath = root.resolve(expectedFile).normalize();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("peId", String.valueOf(pe.getId()));
            entry.put("templateId", pe.getTemplateId());
            entry.put("fileName", expectedFile);
            entry.put("tier", pe.getTier());
            entry.put("inputs", pe.getInputs());
            entry.put("output", pe.getOutput());

            if (Files.exists(schemaPath) && schemaPath.startsWith(root)) {
                String schemaContent = Files.readString(schemaPath);
                entry.put("found", true);
                entry.put("schema", schemaContent);
            } else {
                entry.put("found", false);
            }
            items.add(entry);
        }

        response.put("count", items.size());
        response.put("items", items);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
