package com.dapm.security_service.services;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import com.nimbusds.jwt.SignedJWT;

public class JwtTokenDecoder implements JwtDecoder {
    private final Map<String, RSAPublicKey> publicKeys;
    public JwtTokenDecoder(Map<String, RSAPublicKey> publicKeys) {
        this.publicKeys = publicKeys;
    }
    @Override
    
    public Jwt decode(String token) throws JwtException {
        try {
            // Parse the token without verifying the signature to extract claims
            SignedJWT signedJWT = SignedJWT.parse(token);
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();

            if (issuer == null || !publicKeys.containsKey(issuer)) {
                throw new JwtException("Unknown or missing issuer: " + issuer);
            }

            RSAPublicKey publicKey = publicKeys.get(issuer);
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
            return jwtDecoder.decode(token);
        } catch (ParseException e) {
            throw new JwtException("Failed to parse JWT", e);
        }
    }
}
