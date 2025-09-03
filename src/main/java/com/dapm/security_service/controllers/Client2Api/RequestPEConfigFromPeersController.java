package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.PublisherOrganization;
import com.dapm.security_service.models.dtos2.GetPeerRequest;
import com.dapm.security_service.models.enums.Tier;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.PublisherOrganizationRepository;
import com.dapm.security_service.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/externalPeerConfigs")
public class RequestPEConfigFromPeersController {

    @Value("${runtime.configs.root:/runtime-configs}")
    private String rootDir;

    @Value("${peer.sync.scheme:http}")
    private String syncScheme;

    @Value("${peer.sync.port:8080}")
    private int syncPort;

    // 🔹 updated to point to JSON endpoint instead of /zip
    @Value("${peer.sync.path:/peer/availablePeConfigs}")
    private String syncPath;

    @Autowired
    private ProcessingElementRepository processingElementRepository;

    @Autowired
    private PublisherOrganizationRepository publisherOrganizationRepository;

    @Autowired
    private TokenService tokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    private String buildPeerUrl(String orgName) {
        return String.format("%s://%s:%d%s", syncScheme, orgName.toLowerCase(), syncPort, syncPath);
    }

    @PostMapping("/sync-peer-configs")
    public ResponseEntity<?> syncFromPeer(@RequestBody SyncRequest req) throws Exception {
        String url = buildPeerUrl(req.orgName());

        // 🔑 Generate token for handshake
        String jwtA = tokenService.generateHandshakeToken(300);
        GetPeerRequest body = new GetPeerRequest();
        body.setToken(jwtA);

        // 🔑 Call peer API expecting JSON response
        PeerConfigsResponse response = restTemplate.postForObject(url, body, PeerConfigsResponse.class);

        if (response == null || response.items == null || response.items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new SyncResult(0, List.of(), "Peer returned no configs"));
        }

        // Ensure target dir exists
        Path root = Path.of(rootDir).toAbsolutePath().normalize();
        Files.createDirectories(root);

        List<String> savedFiles = new ArrayList<>();

        for (PeerConfigItem item : response.items) {
            if (!item.found || item.schema == null) {
                continue; // skip missing files
            }

            // Save file to runtime-configs
            Path dest = root.resolve(item.fileName).normalize();
            if (!dest.startsWith(root)) continue; // prevent path traversal
            Files.createDirectories(dest.getParent());
            Files.writeString(dest, item.schema, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            savedFiles.add(item.fileName);

            // Save PE metadata to DB
            PublisherOrganization org = publisherOrganizationRepository.findByName(req.orgName())
                    .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + req.orgName()));

            com.dapm.security_service.models.ProcessingElement peB =
                    com.dapm.security_service.models.ProcessingElement.builder()
                            .id(UUID.randomUUID())
                            .ownerPartnerOrganization(org)
                            .templateId(item.templateId)
                            .tier(item.tier)
                            .instanceNumber(0)
                            .inputs(item.inputs)
                            .output(item.output)
                            .hostURL("http://"+org.getName().toLowerCase()+":8080")
                            .build();

            processingElementRepository.save(peB);
        }

        return ResponseEntity.ok(new SyncResult(savedFiles.size(), savedFiles, "ok"));
    }

    // Request/Response DTOs
    public record SyncRequest(String orgName) {}
    public record SyncResult(int savedCount, List<String> savedFiles, String status) {}

    // Peer response mapping
    public static class PeerConfigsResponse {
        public String organization;
        public int count;
        public List<PeerConfigItem> items;
    }

    public static class PeerConfigItem {
        public String peId;
        public String templateId;
        public String fileName;
        public boolean found;
        public String schema;
        public Tier tier;
        public Set<String> inputs;
        public String output;
    }
}
