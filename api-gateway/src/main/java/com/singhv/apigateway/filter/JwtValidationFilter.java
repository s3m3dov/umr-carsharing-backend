package com.singhv.apigateway.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/health",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.info("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // Call auth-service to validate token
        return validateTokenWithAuthService(token)
                .flatMap(response -> {
                    if (response.isValid()) {
                        log.info("Token validated successfully for user: {}", response.getEmailId());

                        // Add user info to request headers for downstream services
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Email", response.getEmailId())
                                .header("X-User-Id", response.getEmailId().toLowerCase())
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } else {
                        log.warn("Invalid token for path: {}", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error validating token: {}", error.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private Mono<TokenValidationResponse> validateTokenWithAuthService(String token) {
        return webClientBuilder.build()
                .post()
                .uri("lb://auth-service/api/auth/validate")
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .doOnError(error -> log.error("Failed to call auth-service: {}", error.getMessage()));
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1; // High priority - execute before routing
    }

    // Inner DTOs for token validation
    @Getter
    private static class TokenValidationRequest {
        private String token;

        public TokenValidationRequest(String token) {
            this.token = token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private static class TokenValidationResponse {
        private String emailId;
        private boolean isValid;

        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }
    }
}

