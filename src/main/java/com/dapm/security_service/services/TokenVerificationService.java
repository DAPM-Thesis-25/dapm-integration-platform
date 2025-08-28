package com.dapm.security_service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
public class TokenVerificationService {

    private final PublicKeysService publicKeysService;

    public TokenVerificationService(PublicKeysService publicKeysService) {
        this.publicKeysService = publicKeysService;
    }

    /**
     * Verifies the JWT and returns the organization ID ("iss" claim).
     */
    public String verifyTokenAndGetOrganization(String token) {
        try {
            // 1. Extract org + kid from token (without verifying)
            String orgId = PublicKeysService.extractIssuerFromToken(token);
            String kid   = PublicKeysService.extractKidFromToken(token);

            if (orgId == null || orgId.isBlank()) {
                throw new RuntimeException("Missing 'iss' claim in token");
            }

            // 2. Fetch the public key dynamically from JWKS
            PublicKey key = publicKeysService.getKeyForOrg(orgId, kid);

            System.out.println("Verifying token for org " + orgId + " with key " + key);

            // 3. Parse & verify token with the correct key
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return orgId; // return orgId from claims

        } catch (Exception e) {
            throw new RuntimeException("Token verification failed", e);
        }
    }

    /**
     * Verifies an external user's token is signed by their org
     * AND that the org claim matches the claimedOrgId.
     */
    public boolean verifyExternalUser(String token, String claimedOrgId) {
        try {
            // 1. Extract orgId and kid
            String orgId = PublicKeysService.extractIssuerFromToken(token);
            String kid   = PublicKeysService.extractKidFromToken(token);

            if (!claimedOrgId.equals(orgId)) {
                return false; // mismatch between claimed org and token issuer
            }

            // 2. Fetch the org's public key from JWKS
            PublicKey key = publicKeysService.getKeyForOrg(orgId, kid);

            // 3. Verify token signature
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 4. Ensure "organization" claim also matches
            Object tokenOrgId = claims.get("organization");
            return claimedOrgId.equals(tokenOrgId);

        } catch (Exception e) {
            return false;
        }
    }
}
