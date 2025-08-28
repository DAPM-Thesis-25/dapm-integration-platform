package com.dapm.security_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;

@Configuration
public class KeyConfig {

    @Bean
    public KeyPair signingKeyPair(
            @Value("${org.signing.keystore.path}") String path,
            @Value("${org.signing.keystore.password}") String pass,
            @Value("${org.signing.keystore.alias}") String alias) {

        try (InputStream is = Files.newInputStream(Path.of(path))) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(is, pass.toCharArray());

            PrivateKey priv = (PrivateKey) ks.getKey(alias, pass.toCharArray());
            var pub = ks.getCertificate(alias).getPublicKey();

            return new KeyPair(pub, priv);

        } catch (Exception e) {
            throw new IllegalStateException("Cannot load signing keypair", e);
        }
    }
}

