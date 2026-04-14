package com.familyhub.api.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        jwtProvider = new JwtProvider((RSAPrivateKey) pair.getPrivate(), (RSAPublicKey) pair.getPublic());
    }

    @Test
    void generateAccessToken_contains_memberId_and_email_claims() {
        String token = jwtProvider.generateAccessToken(1L, "test@example.com");
        Claims claims = jwtProvider.parseAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
    }

    @Test
    void parseAccessToken_throws_on_tampered_token() {
        String token = jwtProvider.generateAccessToken(1L, "test@example.com");
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsig";

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(tampered))
                .isInstanceOf(Exception.class);
    }
}
