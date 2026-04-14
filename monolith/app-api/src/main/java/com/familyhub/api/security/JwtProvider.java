package com.familyhub.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

public class JwtProvider {

    private static final long ACCESS_TOKEN_EXPIRE_MS = 15 * 60 * 1000L; // 15분

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey  publicKey;

    public JwtProvider(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey  = publicKey;
    }

    public String generateAccessToken(Long memberId, String email) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_MS))
                .signWith(privateKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
