package com.dapm.security_service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenVerificationService {

    private final PublicKeysService publicKeysService;

    private final PublicKey publicKey;
    private final Set<String> usedJtis = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> revokedJtis = Collections.synchronizedSet(new HashSet<>());

//    private final Map<String, Instant> revokedJtis = new ConcurrentHashMap<>();



    public TokenVerificationService(PublicKeysService publicKeysService, KeyPair signingKeyPair) {
        this.publicKeysService = publicKeysService;
        this.publicKey = signingKeyPair.getPublic();
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
    /**
     *
     * Verify a JWT that was issued by *this service* (using local key).
     */
    public Claims verifyAndExtractClaims(String token) {

            String token2 =verifyTokenAndGetTokenClaim(token);

            // throw if
        if (token2 == null || token2.isBlank()) {
            throw new RuntimeException("Token verification failed");
        }
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token2);

            Claims claims = jws.getBody();
            String jti = claims.getId();

            // check if jti is revoked
            if (jti != null && revokedJtis.contains(jti)) {
                throw new RuntimeException("Token has been revoked");
            }

            // Enforce single-use via jti
            if (jti == null || usedJtis.contains(jti)) {
                throw new RuntimeException("Replay detected or missing jti");
            }
            usedJtis.add(jti);

            return claims;
    }

    public String verifyTokenAndGetTokenClaim(String token) {
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

            // 4. Extract the "token" claim
            Claims claims = jws.getBody();
            String tokenClaim = claims.get("token", String.class);

            if (tokenClaim == null || tokenClaim.isBlank()) {
                throw new RuntimeException("Missing 'token' claim in JWT");
            }

            return tokenClaim;

        } catch (Exception e) {
            throw new RuntimeException("Token verification failed", e);
        }
    }


    public void revokeJti(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);

        Claims claims = jws.getBody();
        String jti = claims.getId();
        revokedJtis.add(jti);
    }
}
