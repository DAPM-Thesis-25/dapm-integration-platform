package com.dapm.security_service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Service
public class TokenVerificationService {

    @Autowired
    private PublicKeysService publicKeysService;

    /**
     * @param token The JWT token string.
     * @return The organization identifier (e.g., "OrgA") whose public key validated the token.
     * @throws RuntimeException if token verification fails.
     */
    public String verifyTokenAndGetOrganization(String token) {
        Map<String, PublicKey> keys = publicKeysService.getAllPublicKeys();
        for (Map.Entry<String, PublicKey> entry : keys.entrySet()) {
            try {
                // Try to parse and validate the token with this public key.
                Jws<Claims> jws = Jwts.parserBuilder()
                        .setSigningKey(entry.getValue())
                        .build()
                        .parseClaimsJws(token);
                // If no exception, the signature is valid.
                String issuer = jws.getBody().getIssuer();
                if (issuer == null || issuer.isBlank()) {
                    throw new RuntimeException("Token verified but missing 'iss' claim.");
                }
                return issuer;
            } catch (Exception e) {
                // Verification failed with this key; try the next one.
            }
        }
        throw new RuntimeException("Token verification failed: no matching organization public key found.");
    }



    // verify external user who is requesting access to a resource is really from the org he is claiming from
    public boolean verifyExternalUser(String token, String claimedOrgId) {
        System.out.println("verify me " + token);
        Map<String, PublicKey> keys = publicKeysService.getAllPublicKeys();

        for (Map.Entry<String, PublicKey> entry : keys.entrySet()) {
            try {
                // Step 1: Try verifying the token with this org's key
                var claims = Jwts.parserBuilder()
                        .setSigningKey(entry.getValue())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Step 2: Check that this org is the one the token claims to be from
                Object tokenOrgId = claims.get("organization");
                System.out.println("I am claim "+ tokenOrgId);

                if (tokenOrgId != null &&
                        tokenOrgId.equals(claimedOrgId) &&
                        entry.getKey().equals(claimedOrgId)) {
                    System.out.println("I am verified with org "+ entry.getKey());
                    return true; // Signature matches AND orgId matches
                }

            } catch (Exception e) {
                // Try next key
            }
        }

        // No matching org's key verified the token AND matched the claim
        return false;
    }

}
