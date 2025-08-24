package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.controllers.PeerApi.HandshakeController;
import com.dapm.security_service.models.PartnerOrganization;
import com.dapm.security_service.repositories.PartnerOrganizationRepository;
import com.dapm.security_service.security.CustomUserDetails;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/handshake")
public class HandshakeClientController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenVerificationService verificationService;

    // Instead of a fixed URL, keep only the static parts configurable.
    // We'll inject orgName at runtime to form: {scheme}://{orgName}:{port}{path}
    @Value("${peer.handshake.scheme:http}")
    private String handshakeScheme;

    @Value("${peer.handshake.port:8080}")
    private int handshakePort;

    @Value("${peer.handshake.path:/api/peer/handshake}")
    private String handshakePath;

    @Autowired
    private PartnerOrganizationRepository partnerOrganizationRepository;

    @PostMapping("")
    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    public ResponseEntity<Map<String, Object>> createHandshake(
            @RequestBody HandshakeResult.OrgNameRequest orgName,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> body = new HashMap<>();

        if (orgName == null || orgName.orgName.isBlank()) {
            body.put("success", false);
            body.put("message", "Organization name is required in the request body.");
            return ResponseEntity.badRequest().body(body);
        }

        try {
            HandshakeResult result = sendHandshake(orgName.orgName.trim());

            if (result.success) {
                body.put("success", true);
                body.put("message", "Partnership established.");
                body.put("requestedOrg", orgName.orgName().trim());
                body.put("verifiedOrg", result.verifiedOrg);
                body.put("details", "Handshake completed and partner recorded.");
                return ResponseEntity.ok(body);
            } else {
                body.put("success", false);
                body.put("message", "Failed to establish partnership.");
                body.put("requestedOrg", orgName.orgName().trim());
                body.put("error", result.errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
            }
        } catch (Exception ex) {
            body.put("success", false);
            body.put("message", "Unexpected error during handshake.");
            body.put("requestedOrg", orgName.orgName().trim());
            body.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    private String buildPeerUrl(String orgName) {
        // Example: http://{orgName}:8080/api/peer/handshake
        return String.format("%s://%s:%d%s", handshakeScheme, orgName.toLowerCase(), handshakePort, handshakePath);
    }

    private synchronized HandshakeResult sendHandshake(String orgName) {
        try {
            // a) Create handshake token
            String jwtA = tokenService.generateHandshakeToken(300);

            // b) Build request payload
            HandshakeController.HandshakeRequest req = new HandshakeController.HandshakeRequest();
            req.setToken(jwtA);

            // c) Call peer handshake endpoint with dynamic org host
            String url = buildPeerUrl(orgName);
            HandshakeController.HandshakeResponse resp =
                    restTemplate.postForObject(url, req, HandshakeController.HandshakeResponse.class);

            if (resp == null || resp.getToken() == null) {
                return HandshakeResult.fail("Peer response was empty or missing token.");
            }

            // d) Verify peer's token and extract their org
            String responseOrg = verificationService.verifyTokenAndGetOrganization(resp.getToken());
            System.out.println("I am the verified"+responseOrg);
            if (responseOrg != null) {
                partnerOrganizationRepository.findByName(responseOrg)
                        .orElseGet(() -> partnerOrganizationRepository.save(
                                new PartnerOrganization(UUID.randomUUID(), responseOrg)
                        ));
                return HandshakeResult.ok(responseOrg);
            }

            return HandshakeResult.fail("Token verification failed or organization was null.");
        } catch (RestClientException rce) {
            return HandshakeResult.fail("HTTP error calling peer: " + rce.getMessage());
        } catch (Exception ex) {
            return HandshakeResult.fail("Handshake failed: " + ex.getMessage());
        }
    }

    // Simple internal result holder (no new file)
    private static class HandshakeResult {
        final boolean success;
        final String verifiedOrg;
        final String errorMessage;

        private HandshakeResult(boolean success, String verifiedOrg, String errorMessage) {
            this.success = success;
            this.verifiedOrg = verifiedOrg;
            this.errorMessage = errorMessage;
        }

        static HandshakeResult ok(String verifiedOrg) {
            return new HandshakeResult(true, verifiedOrg, null);
        }

        static HandshakeResult fail(String errorMessage) {
            return new HandshakeResult(false, null, errorMessage);
        }
        public record OrgNameRequest(String orgName) {}
    }
}
