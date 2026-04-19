package com.familyhub.api.auth;

import com.familyhub.api.auth.dto.*;
import com.familyhub.api.exception.AppException;
import com.familyhub.api.exception.ErrorCode;
import com.familyhub.api.security.JwtProvider;
import com.familyhub.db.member.MemberJpaRepository;
import com.familyhub.domain.member.Member;
import com.familyhub.redis.auth.RefreshTokenRepository;
import com.familyhub.redis.auth.TempCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    MemberJpaRepository memberRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    JwtProvider jwtProvider;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    TempCodeRepository tempCodeRepository;
    @InjectMocks
    AuthService authService;
    @Mock
    TempCodeRepository tempCodeRepository;

    @Test
    void signup_saves_member_and_returns_response() {
        var req = new SignupRequest("test@test.com", "password123", "홍길동");
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(memberRepository.save(any())).thenReturn(Member.create("test@test.com", "encoded", "홍길동"));

        SignupResponse response = authService.signup(req);

        assertThat(response.email()).isEqualTo("test@test.com");
        verify(memberRepository).save(any());
    }

    @Test
    void signup_throws_EMAIL_ALREADY_EXISTS_when_duplicate() {
        var req = new SignupRequest("test@test.com", "password123", "홍길동");
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(AppException.class)
                .matches(e -> ((AppException) e).getErrorCode() == ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void login_throws_INVALID_CREDENTIALS_when_password_wrong() {
        var req = new LoginRequest("test@test.com", "wrong");
        Member member = Member.create("test@test.com", "encoded", "홍길동");
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AppException.class)
                .matches(e -> ((AppException) e).getErrorCode() == ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_throws_INVALID_CREDENTIALS_when_member_not_found() {
        var req = new LoginRequest("nobody@test.com", "pw");
        when(memberRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AppException.class)
                .matches(e -> ((AppException) e).getErrorCode() == ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void refresh_throws_REFRESH_TOKEN_NOT_FOUND_when_token_missing() {
        var req = new RefreshRequest("unknown-token");
        when(refreshTokenRepository.findMemberIdByTokenId("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(AppException.class)
                .matches(e -> ((AppException) e).getErrorCode() == ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    void logout_deletes_refresh_token() {
        var req = new LogoutRequest("token-id");
        authService.logout(req);
        verify(refreshTokenRepository).delete("token-id");
    }

    @Test
    void exchangeToken_returns_tokens_when_code_valid() {
        var req = new TempCodeRequest("valid-code");
        when(tempCodeRepository.findAndDelete("valid-code")).thenReturn(Optional.of("42"));
        Member member = Member.create("test@test.com", "encoded", "홍길동");
        when(memberRepository.findById(42L)).thenReturn(Optional.of(member));
        when(jwtProvider.generateAccessToken(any(), any())).thenReturn("access.token");

        TokenResponse response = authService.exchangeToken(req);

        assertThat(response.accessToken()).isEqualTo("access.token");
        verify(refreshTokenRepository).save(any(), eq("42"));
    }

    @Test
    void exchangeToken_throws_INVALID_AUTH_CODE_when_code_not_found() {
        var req = new TempCodeRequest("expired-code");
        when(tempCodeRepository.findAndDelete("expired-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.exchangeToken(req))
                .isInstanceOf(AppException.class)
                .matches(e -> ((AppException) e).getErrorCode() == ErrorCode.INVALID_AUTH_CODE);
    }
}
