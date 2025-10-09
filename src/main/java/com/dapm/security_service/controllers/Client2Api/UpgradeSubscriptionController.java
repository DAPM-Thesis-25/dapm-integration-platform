package com.dapm.security_service.controllers.Client2Api;

import com.dapm.security_service.models.PublisherOrganization;
import com.dapm.security_service.models.dtos2.SubscriptionRequestDto;
import com.dapm.security_service.models.enums.SubscriptionTier;
import com.dapm.security_service.repositories.PublisherOrganizationRepository;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@RestController
@RequestMapping("/api/upgrade-subscription")
public class UpgradeSubscriptionController {

    @Autowired private TokenService tokenService;
    @Autowired private TokenVerificationService verificationService;
    @Autowired private PublisherOrganizationRepository publisherRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${peer.handshake.scheme:http}")
    private String handshakeScheme;

    @Value("${peer.handshake.port:8080}")
    private int handshakePort;

    @Value("${peer.handshake.path:/api/peer/upgrade-subscription}")
    private String handshakePath;

    private String buildPeerUrl(String orgName) {
        return String.format("%s://%s:%d%s", handshakeScheme, orgName.toLowerCase(), handshakePort, handshakePath);
    }

    @PostMapping
    public ResponseEntity<PeerUpgradeResponse> requestSubscriptionUpgrade(@RequestBody SubscriptionRequestDto dto) {
        try {
            // 1) Generate handshake token
            String token = tokenService.generateHandshakeToken(300);
            dto.setToken(token);

            // 2) Call peer
            String url = buildPeerUrl(dto.getOrgName());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SubscriptionRequestDto> entity = new HttpEntity<>(dto, headers);

            ResponseEntity<PeerUpgradeResponse> response =
                    restTemplate.postForEntity(url, entity, PeerUpgradeResponse.class);

            // 3) Persist on success
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().success) {
                System.out.println("Subscription upgrade successful: " + response.getBody().message() +
                        " new tier=" + response.getBody().tier() + " at " + Instant.now());

                PublisherOrganization publisherOrganization = publisherRepository.findByName(dto.getOrgName())
                        .orElseThrow(() -> new IllegalArgumentException("Partner Organization not found or handshake not completed."));
                publisherOrganization.setTier(response.getBody().tier());
                publisherOrganization.setMaxHours(response.getBody().maxHours());
                publisherRepository.save(publisherOrganization);
            }

            // 4) Bubble up the peer’s response as-is
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (HttpStatusCodeException ex) {
            // Peer returned 4xx/5xx. Parse its JSON and forward cleanly.
            String body = ex.getResponseBodyAsString();
            try {
                // Try to deserialize directly into our record
                PeerUpgradeResponse parsed = objectMapper.readValue(body, PeerUpgradeResponse.class);
                return ResponseEntity.status(ex.getStatusCode()).body(parsed);
            } catch (Exception ignored) {
                // Fallback: pull "message" if available, else a generic message
                String cleanMessage = extractMessageField(body);
                return ResponseEntity.status(ex.getStatusCode())
                        .body(new PeerUpgradeResponse(false, cleanMessage, null, null));
            }
        } catch (Exception e) {
            // Truly unexpected error on our side
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PeerUpgradeResponse(false, "Unexpected error while upgrading subscription", null, null) );
        }
    }

    private String extractMessageField(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode messageNode = node.get("message");
            if (messageNode != null && messageNode.isTextual()) {
                return messageNode.asText();
            }
        } catch (Exception ignored) {}
        return "Request failed";
    }

    // Internal DTO to match Peer API response
    static record PeerUpgradeResponse(boolean success, String message, SubscriptionTier tier, Integer maxHours) {}
}
