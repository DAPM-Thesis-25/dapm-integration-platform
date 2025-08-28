package com.dapm.security_service.controllers.PeerApi2;

import com.dapm.security_service.models.ProcessingElement;
import com.dapm.security_service.models.SubscriberOrganization;
import com.dapm.security_service.models.dtos2.GetPeerRequest;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.SubscriberOrganizationRepository;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


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


    @PostMapping("/available-pe-configs")
    public ResponseEntity<?> requestPeConfigs(@RequestBody GetPeerRequest request) {
        // 1. Verify incoming token and extract callerOrg
        String callerOrg = verificationService.verifyTokenAndGetOrganization(request.getToken());
        if (callerOrg == null || callerOrg.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid or missing token.");
        }
        SubscriberOrganization partnerOrg = partnerOrganizationRepository.findByName(callerOrg)
                .orElseThrow(() -> new IllegalArgumentException("Partner Organization not found or handshake not completed."));

        // 2. Fetch visible PE templates
        List<ProcessingElement> visiblePEs = peRepository
                .findByVisibilityContaining(callerOrg);

        // 3. Root dir (same as ConfigSchemaController.root())
        Path root = Path.of("/runtime-configs").toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            return ResponseEntity.ok(List.of()); // no configs yet
        }

        return ResponseEntity.ok("resp");
    }

    @PostMapping("/availablePeConfigs/zip")
    public ResponseEntity<Resource> requestPeConfigsZip(@RequestBody GetPeerRequest request) throws Exception {
        System.out.println("I received a request for PE configs ZIP");
        // 1) Verify org from token
        String callerOrg = verificationService.verifyTokenAndGetOrganization(request.getToken());
        if (callerOrg == null || callerOrg.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println("Caller organization: " + callerOrg);
        partnerOrganizationRepository.findByName(callerOrg)
                .orElseThrow(() -> new IllegalArgumentException("Partner Organization not found or handshake not completed."));

        // 2) Visible PEs
        List<ProcessingElement> visiblePEs = peRepository.findByVisibilityContaining(callerOrg);

        // 3) Schema root (same place ConfigSchemaController uses)
        Path root = Path.of("/runtime-configs").toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            // return an empty zip
            ByteArrayResource empty = new ByteArrayResource(new byte[0]);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schemas.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(empty);
        }

        // 4) Stream a ZIP in-memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            for (ProcessingElement pe : visiblePEs) {
                String expectedFile = orgName.toLowerCase() + "_" + pe.getTemplateId().toLowerCase() + "_config_schema.json";
                Path schemaPath = root.resolve(expectedFile).normalize();
                if (Files.exists(schemaPath) && schemaPath.startsWith(root)) {
                    zos.putNextEntry(new java.util.zip.ZipEntry(expectedFile));
                    Files.copy(schemaPath, zos);
                    zos.closeEntry();
                }
            }
        }
        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schemas.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


}
