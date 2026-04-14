package com.familyhub.api.config;

import com.familyhub.api.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public JwtProvider jwtProvider(
            @Value("${jwt.private-key-path}") Resource privateKeyResource,
            @Value("${jwt.public-key-path}")  Resource publicKeyResource
    ) throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey(privateKeyResource.getInputStream());
        RSAPublicKey  publicKey  = loadPublicKey(publicKeyResource.getInputStream());
        return new JwtProvider(privateKey, publicKey);
    }

    private RSAPrivateKey loadPrivateKey(InputStream is) throws Exception {
        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String key = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(key);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(bytes));
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
