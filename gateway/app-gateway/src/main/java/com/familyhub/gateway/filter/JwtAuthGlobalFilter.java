package com.familyhub.gateway.filter;

import com.familyhub.gateway.security.JwtVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> SKIP_PATHS = List.of(
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/token"
    );

    private static final String OAUTH2_PATH_PREFIX = "/api/v1/auth/oauth2/";

    private final JwtVerifier jwtVerifier;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (SKIP_PATHS.contains(path) || path.startsWith(OAUTH2_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            jwtVerifier.verify(authHeader.substring(7));
            return chain.filter(exchange);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
