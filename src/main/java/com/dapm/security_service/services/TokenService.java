package com.dapm.security_service.services;

import com.dapm.security_service.models.PipelineProcessingElementRequest;
import com.dapm.security_service.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.Instant;
import java.util.*;

@Service
public class TokenService {

    private final KeyPair signingKeyPair;
    private final String orgId;
    private final String kidValue;

    public TokenService(KeyPair signingKeyPair,
                        @Value("${dapm.orgId}") String orgId,
                        @Value("${dapm.jwt.kid}") String kidValue) {
        this.signingKeyPair = signingKeyPair;
        this.orgId = orgId;
        this.kidValue = kidValue;
    }

    /**
     * Generates a JWT for a given user including org + username claims.
     */
    public String generateTokenForUser(User user, long expirationMillis) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("organization", user.getOrganization().getName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuer(orgId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam("kid", kidValue)
                .compact();
    }

    /**
     * Generates a token for a pipeline node request.
     */
    public String generateTokenForNodeRequest(PipelineProcessingElementRequest request) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("pipelineId", request.getPipelineName());
//        claims.put("pipelineNodeId", request.getProcessingElement().getId().toString());
        claims.put("requesterIdUsername", request.getRequesterInfo().getRequesterId());
//        claims.put("allowedExecutions", request.getRequestedExecutionCount());
        claims.put("allowedDurationHours", request.getRequestedDurationHours());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(request.getId().toString())
                .setIssuer(orgId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(request.getRequestedDurationHours())))
                .signWith(signingKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam("kid", kidValue)
                .compact();
    }

    /**
     * Generates a JWT for a partner org user.
     */
    public String generateTokenForPartnerOrgUser(User user, long ttlSeconds) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("organization", user.getOrganization().getName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuer(orgId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(signingKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam("kid", kidValue)
                .compact();
    }

    /**
     * Generates the handshake JWT used for inter-org trust establishment.
     */
    public String generateHandshakeToken(long ttlSeconds) {
        Instant now = Instant.now();

        String token = Jwts.builder()
                .setIssuer(orgId)
                .setSubject("handshake")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .setId(UUID.randomUUID().toString())
                .signWith(signingKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .setHeaderParam("kid", kidValue)
                .compact();

        System.out.println("[HandshakeToken] Org=" + orgId + " kid=" + kidValue);
        System.out.println("[HandshakeToken] Token=" + token);

        return token;
    }
}
