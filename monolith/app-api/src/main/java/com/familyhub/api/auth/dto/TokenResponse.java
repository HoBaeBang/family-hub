package com.familyhub.api.auth.dto;

public record TokenResponse(String accessToken, String refreshToken, int expiresIn) {
}
