package com.familyhub.gateway.filter;

import com.familyhub.gateway.security.JwtVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthGlobalFilterTest {

    @Mock JwtVerifier jwtVerifier;
    @Mock GatewayFilterChain chain;
    JwtAuthGlobalFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthGlobalFilter(jwtVerifier);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void signup_path_bypasses_jwt_verification() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/auth/signup").build()
        );
        filter.filter(exchange, chain).block();

        verify(jwtVerifier, never()).verify(any());
        verify(chain).filter(exchange);
    }

    @Test
    void login_path_bypasses_jwt_verification() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/auth/login").build()
        );
        filter.filter(exchange, chain).block();

        verify(jwtVerifier, never()).verify(any());
    }

    @Test
    void refresh_path_bypasses_jwt_verification() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/auth/refresh").build()
        );
        filter.filter(exchange, chain).block();

        verify(jwtVerifier, never()).verify(any());
    }

    @Test
    void missing_authorization_header_returns_401() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/workspaces").build()
        );
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void invalid_token_returns_401() {
        doThrow(new RuntimeException("invalid")).when(jwtVerifier).verify("bad.token");
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/workspaces")
                .header("Authorization", "Bearer bad.token")
                .build()
        );
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void valid_token_passes_through_to_chain() {
        doNothing().when(jwtVerifier).verify("good.token");
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/workspaces")
                .header("Authorization", "Bearer good.token")
                .build()
        );
        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }
}
