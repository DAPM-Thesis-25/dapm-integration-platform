package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.dtos.ProcessingElementDto;
import com.dapm.security_service.repositories.ProcessingElementRepository;
import com.dapm.security_service.services.PublicKeysService;
import com.dapm.security_service.services.TokenService;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/keys")
public class PublicKeyTestController {
    @Autowired
    private final PublicKeysService publicKeysService;

    @Autowired
    public PublicKeyTestController(PublicKeysService publicKeysService) {
        this.publicKeysService = publicKeysService;
    }

    @GetMapping("/public-key-test")
    public ResponseEntity<String> testKey(@RequestParam String orgId) {
        if (orgId == null || orgId.isBlank()) {
            return ResponseEntity.badRequest().body("Organization ID is required");
        }

        return publicKeysService.getPublicKey(orgId) != null
                ? ResponseEntity.ok("Key loaded successfully")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found for organization: " + orgId);
    }

    @PostMapping("/test-key-pair")
    public ResponseEntity<String> testKeyPair(@RequestParam String orgId, @RequestBody String privateKeyPem) {
        if (orgId == null || orgId.isBlank() || privateKeyPem == null || privateKeyPem.isBlank()) {
            return ResponseEntity.badRequest().body("Organization ID and Private Key are required");
        }
        try {
            boolean valid = publicKeysService.testKeyPair(orgId, privateKeyPem);
            if (valid) {
                return ResponseEntity.ok("Key pair is valid for organization: " + orgId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The provided private key does NOT match the stored public key for organization: " + orgId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error testing key pair: " + e.getMessage());
        }
    }


}