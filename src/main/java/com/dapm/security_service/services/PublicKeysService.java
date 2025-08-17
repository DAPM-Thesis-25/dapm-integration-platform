package com.dapm.security_service.services;

import com.dapm.security_service.config.OrganizationKeysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PublicKeysService {
    private final Map<String, PublicKey> publicKeys;
    private final String keyAlgorithm;

    @Autowired
    public PublicKeysService(OrganizationKeysConfig config,
                             @Value("${org.security.key-algorithm:RSA}") String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        this.publicKeys = Collections.unmodifiableMap(
                config.getKeys().entrySet().stream()
                        .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> parsePublicKey(e.getValue())
                                )
                        ));
    }

    public PublicKey getPublicKey(String organizationId) {
        return publicKeys.get(organizationId);
    }

    public Map<String, PublicKey> getAllPublicKeys() {
        return publicKeys;
    }

    private PublicKey parsePublicKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new SecurityException("Empty PEM string provided");
        }

        String publicKeyPEM = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        try {
            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            return createPublicKey(encoded);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Base64 decoding failed", e);
        }
    }

    private PublicKey createPublicKey(byte[] encodedKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Unsupported algorithm: " + keyAlgorithm, e);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException("Invalid key specification", e);
        }
    }

    private PrivateKey parsePrivateKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new SecurityException("Empty PEM string provided for private key");
        }

        String privateKeyPEM = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Unsupported algorithm: " + keyAlgorithm, e);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException("Invalid private key specification", e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Base64 decoding failed for private key", e);
        }
    }

    public boolean testKeyPair(String organizationId, String privateKeyPem) {
        PublicKey publicKey = getPublicKey(organizationId);
        if (publicKey == null) {
            throw new SecurityException("No public key found for organization: " + organizationId);
        }
        PrivateKey privateKey = parsePrivateKey(privateKeyPem);

        try {
            // Use SHA256withRSA (adjust if using a different algorithm)
            Signature signature = Signature.getInstance("SHA256withRSA");
            String testMessage = "Test message for key validation";

            // Sign with the private key
            signature.initSign(privateKey);
            signature.update(testMessage.getBytes(StandardCharsets.UTF_8));
            byte[] signedData = signature.sign();

            // Verify with the public key
            signature.initVerify(publicKey);
            signature.update(testMessage.getBytes(StandardCharsets.UTF_8));

            return signature.verify(signedData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new SecurityException("Error during key pair testing", e);
        }
    }

}