package com.dapm.security_service.controllers.PeerApi2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
public class JwksController {

    private final KeyPair signingKeyPair;
    private final String kid;

    public JwksController(KeyPair signingKeyPair,
                          @Value("${dapm.jwt.kid}") String kid) {
        this.signingKeyPair = signingKeyPair;
        this.kid = kid;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) signingKeyPair.getPublic();

        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getPublicExponent().toByteArray());

        Map<String, Object> jwk = Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", kid,
                "n", n,
                "e", e
        );

        return Map.of("keys", List.of(jwk));
    }
}

