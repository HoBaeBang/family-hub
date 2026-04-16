package com.familyhub.gateway.security;

import io.jsonwebtoken.Jwts;
import java.security.interfaces.RSAPublicKey;

public class JwtVerifier {

    private final RSAPublicKey publicKey;

    public JwtVerifier(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void verify(String token) {
        Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
    }
}
