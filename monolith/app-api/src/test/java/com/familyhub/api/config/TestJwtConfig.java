package com.familyhub.api.config;

import com.familyhub.api.security.JwtProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    @Primary
    public JwtProvider jwtProvider() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        return new JwtProvider((RSAPrivateKey) pair.getPrivate(), (RSAPublicKey) pair.getPublic());
    }
}
