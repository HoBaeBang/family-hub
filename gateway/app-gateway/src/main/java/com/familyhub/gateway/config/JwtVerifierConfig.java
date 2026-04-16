package com.familyhub.gateway.config;

import com.familyhub.gateway.security.JwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtVerifierConfig {

    @Bean
    public JwtVerifier jwtVerifier(@Value("${jwt.public-key-path}") Resource resource) throws Exception {
        return new JwtVerifier(loadPublicKey(resource.getInputStream()));
    }

    private RSAPublicKey loadPublicKey(InputStream is) throws Exception {
        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String key = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(key);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(bytes));
    }
}
