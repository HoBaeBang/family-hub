package com.familyhub.api.auth;

import com.familyhub.api.auth.dto.*;
import com.familyhub.api.auth.oauth2.OAuth2UserService;
import com.familyhub.api.config.SecurityConfig;
import com.familyhub.api.exception.AppException;
import com.familyhub.api.exception.ErrorCode;
import com.familyhub.api.exception.GlobalExceptionHandler;
import com.familyhub.redis.auth.TempCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "oauth2.redirect-uri=http://localhost:3000/auth/callback")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    OAuth2UserService oAuth2UserService;
    @MockitoBean
    TempCodeRepository tempCodeRepository;

    @Test
    void signup_returns_201_with_memberId_and_email() throws Exception {
        when(authService.signup(any())).thenReturn(new SignupResponse(1L, "test@test.com"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("test@test.com", "password123", "홍길동"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void signup_returns_409_when_email_already_exists() throws Exception {
        when(authService.signup(any())).thenThrow(new AppException(ErrorCode.EMAIL_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("test@test.com", "password123", "홍길동"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void signup_returns_400_when_email_format_invalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("not-an-email", "password123", "홍길동"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void login_returns_200_with_tokens() throws Exception {
        when(authService.login(any())).thenReturn(new TokenResponse("access.token", "refresh-uuid", 900));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("test@test.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-uuid"));
    }

    @Test
    void logout_returns_204() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer access.token")
                        .content(objectMapper.writeValueAsString(new LogoutRequest("refresh-uuid"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void token_returns_200_with_tokens_when_code_valid() throws Exception {
        when(authService.exchangeToken(any())).thenReturn(new TokenResponse("access.token", "refresh-uuid", 900));

        mockMvc.perform(get("/api/v1/auth/token")
                        .param("code", "valid-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-uuid"));
    }

    @Test
    void token_returns_401_when_code_invalid() throws Exception {
        when(authService.exchangeToken(any())).thenThrow(new AppException(ErrorCode.INVALID_AUTH_CODE));

        mockMvc.perform(get("/api/v1/auth/token")
                        .param("code", "expired-code"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_AUTH_CODE"));
    }
}
