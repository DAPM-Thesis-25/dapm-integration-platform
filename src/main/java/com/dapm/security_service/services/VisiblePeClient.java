package com.dapm.security_service.services;

import com.dapm.security_service.controllers.PeerApi.PeerHandshakeController.HandshakeRequest;
import com.dapm.security_service.controllers.PeerApi.PeerHandshakeController.HandshakeResponse;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

/**
 * Client for fetching external PE templates via the automated handshake.
 */
@Component
public class VisiblePeClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TokenService tokenService;

    // Handshake endpoint URL (OrgB)
    @Value("${peer.handshake.url:http://orgb:8080/api/peer/handshake}")
    private String handshakeUrl;

    // In‑memory cache
    private List<ProcessingElementDto> cache = List.of();
    private Instant cacheExpiry = Instant.EPOCH;

    public VisiblePeClient(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public synchronized List<ProcessingElementDto> getVisiblePEsFromOrgB(String requestingOrg) {
        // 1) If cache is stale, re‑handshake
        if (cache.isEmpty() || Instant.now().isAfter(cacheExpiry)) {
            // a) Generate OrgA’s handshake JWT_A
            String jwtA = tokenService.generateHandshakeToken(300);

            // b) Build request payload
            HandshakeRequest req = new HandshakeRequest();
            req.setToken(jwtA);

            // c) Call OrgB’s handshake endpoint
            HandshakeResponse resp = restTemplate
                    .postForObject(handshakeUrl, req, HandshakeResponse.class);

            // d) Update cache and expiry (e.g. refresh 60s before actual expiry)
//            cache = resp.getTemplates();
            cacheExpiry = Instant.now().plusSeconds(240); // 4 minutes
        }

        // 2) Return cached list
        return cache;
    }
}
