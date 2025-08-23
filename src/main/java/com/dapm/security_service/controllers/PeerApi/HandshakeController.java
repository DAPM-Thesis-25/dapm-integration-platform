package com.dapm.security_service.controllers.PeerApi;
import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/peer")
public  class HandshakeController {

    private final TokenService tokenService;
    private final TokenVerificationService verificationService;
    private final ProcessingElementRepository peRepository;

    @Autowired
    public HandshakeController(TokenService tokenService,
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
    public ResponseEntity<HandshakeResponse> handshake(@RequestBody HandshakeRequest request) {
        // 1. Verify incoming token and extract callerOrg
        String callerOrg = verificationService.verifyTokenAndGetOrganization(request.getToken());

        // 2. Fetch visible PE templates
        List<ProcessingElementDto> templates = peRepository
                .findByOwnerOrganization_NameOrVisibilityContaining(callerOrg, callerOrg)
                .stream()
                .map(ProcessingElementDto::new)
                .collect(Collectors.toList());

        // 3. Sign response token
        String responseToken = tokenService.generateHandshakeToken(300);  // 5 minutes

        // 4. Build and return payload
        HandshakeResponse resp = new HandshakeResponse(responseToken, templates);
        return ResponseEntity.ok(resp);
    }



    public static class HandshakeRequest {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }


    public static class HandshakeResponse {
        private String token;
        private List<ProcessingElementDto> templates;
        public HandshakeResponse(String token, List<ProcessingElementDto> templates) {
            this.token = token;
            this.templates = templates;
        }
        public String getToken() { return token; }
        public List<ProcessingElementDto> getTemplates() { return templates; }
    }
}