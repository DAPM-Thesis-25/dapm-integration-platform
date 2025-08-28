package com.dapm.security_service.controllers.Client2Api;


import com.dapm.security_service.models.PublisherOrganization;
import com.dapm.security_service.models.dtos2.GetPeerRequest;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.PublisherOrganizationRepository;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/externalPeerConfigs")
public class RequestPEConfigFromPeersController {

    @Value("${runtime.configs.root:/runtime-configs}")
    private String rootDir;


    @Value("${peer.sync.scheme:http}")
    private String syncScheme;

    @Value("${peer.sync.port:8080}")
    private int syncPort;
    @Value("${peer.sync.path:/peer/availablePeConfigs/zip}")
    private String syncPath;

    @Autowired
    private ProcessingElementRepository processingElementRepository;


    @Autowired
    private PublisherOrganizationRepository publisherOrganizationRepository;

    private String buildPeerUrl(String orgName) {
        return String.format("%s://%s:%d%s", syncScheme, orgName.toLowerCase(), syncPort, syncPath);
    }

    @Autowired
    private TokenService tokenService;

    private final RestTemplate rest = new RestTemplate();

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/sync-peer-configs")
    public ResponseEntity<?> syncFromPeer(@RequestBody SyncRequest req) throws Exception {
        String url = buildPeerUrl(req.orgName());

        String jwtA = tokenService.generateHandshakeToken(300);

        GetPeerRequest body = new GetPeerRequest();
        body.setToken(jwtA);

        // 👇 simplified call, same as handshake but expecting bytes
        byte[] zipBytes = restTemplate.postForObject(url, body, byte[].class);

        if (zipBytes == null || zipBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new SyncResult(0, List.of(), "Peer returned empty body"));
        }

        // 2) Unzip into runtime.configs.root
        Path root = Path.of(rootDir).toAbsolutePath().normalize();
        Files.createDirectories(root);

        List<String> saved = new ArrayList<>();
        try (InputStream is = new ByteArrayInputStream(zipBytes);
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                String fileName = Paths.get(entry.getName()).getFileName().toString();
                String[] parts = fileName.split("_");
                if (parts.length >= 3) {
                    String orgName = parts[0];                 // "orgA"
                    String processingElementName = parts[1];   // "pipeline"

                    PublisherOrganization org=publisherOrganizationRepository.findByName(req.orgName())
                            .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + req.orgName()));
                    if(org == null) {
                        return ResponseEntity.badRequest().body("Organization not found: " + req.orgName());
                    }

                    com.dapm.security_service.models.ProcessingElement peB = com.dapm.security_service.models.ProcessingElement.builder()
                            .id(UUID.randomUUID())
                            .ownerPartnerOrganization(org)
                            .templateId(processingElementName)
                            .build();
                    // save the ProcessingElement to the repository
                    processingElementRepository.save(peB);
                }
                Path dest = root.resolve(fileName).normalize();
                if (!dest.startsWith(root)) {
                    zis.closeEntry();
                    continue;
                }
                Files.createDirectories(dest.getParent());
                Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                saved.add(fileName);
                zis.closeEntry();
            }
        }

        return ResponseEntity.ok(new SyncResult(saved.size(), saved, "ok"));
    }


    // Request/Response DTOs
    public record SyncRequest(String orgName) {}
    public record SyncResult(int savedCount, List<String> savedFiles, String status) {}
}