package com.dapm.security_service.controllers.PeerApi;
import com.dapm.security_service.models.SubscriberOrganization;
import com.dapm.security_service.models.Tiers;
import com.dapm.security_service.models.enums.SubscriptionTier;
import com.dapm.security_service.models.enums.Tier;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.repositories.SubscriberOrganizationRepository;
import com.dapm.security_service.repositories.TiersRepository;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/peer")
public  class PeerHandshakeController {

    private final TokenService tokenService;
    private final TokenVerificationService verificationService;
    private final ProcessingElementRepository peRepository;
    @Autowired
    private SubscriberOrganizationRepository subscriberOrganizationRepository;

    @Autowired
    TiersRepository tiersRepository;
    @Autowired
    public PeerHandshakeController(TokenService tokenService,
                                   TokenVerificationService verificationService,
                                   ProcessingElementRepository peRepository) {
        this.tokenService = tokenService;
        this.verificationService = verificationService;
        this.peRepository = peRepository;
    }


    @GetMapping("/handshake/token")
    public ResponseEntity<String> getHandshakeToken() {
        String jwt = tokenService.generateHandshakeToken(300);  // 5 min TTL
        return ResponseEntity.ok(jwt);
    }


    @PostMapping("/handshake")
    public ResponseEntity<?> handshake(@RequestBody HandshakeRequest request) {
        try {
            // 1. Verify incoming token
            String callerOrg = verificationService.verifyTokenAndGetOrganization(request.getToken());

            if (callerOrg != null) {
                // 2. Save caller as partner if not already
                subscriberOrganizationRepository.findByName(callerOrg)
                        .orElseGet(() -> subscriberOrganizationRepository.save(
                                new SubscriberOrganization(UUID.randomUUID(), callerOrg, SubscriptionTier.FREE)
                        ));
                Tiers tier=tiersRepository.findByName(Tier.FREE).orElseThrow(()->new IllegalArgumentException("Tier not found"));

                // 3. Just return OK
                HandshakeResponse response = new HandshakeResponse(
                        tier.getMaxHours()
                );

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Token verification failed.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during handshake: " + e.getMessage());
        }
    }




    public static class HandshakeRequest {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    public static class HandshakeResponse {
        private Integer maxHours;
        public HandshakeResponse(Integer maxHours) {
            this.maxHours = maxHours;
        }
        public Integer getMaxHours() { return maxHours; }
    }

}