package com.cimhub.apigateway.filter;

import com.cimhub.apigateway.config.SecurityProperties;
import com.cimhub.apigateway.config.ServiceProperties;
import com.cimhub.apigateway.constant.GatewayConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private final SecurityProperties securityProperties;
    private final ServiceProperties serviceProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.info("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(GatewayConstants.BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(GatewayConstants.BEARER_PREFIX.length());

        // Call auth-service to validate token
        return validateTokenWithAuthService(token)
                .flatMap(response -> {
                    if (response.isValid()) {
                        log.info("Token validated successfully for user: {}", response.getEmailId());

                        // Add user info to request headers for downstream services
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header(GatewayConstants.USER_EMAIL_HEADER, response.getEmailId())
                                .header(GatewayConstants.USER_ID_HEADER, response.getEmailId().toLowerCase())
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
                .uri(serviceProperties.getAuth().getValidateUrl())
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .doOnError(error -> log.error("Failed to call auth-service: {}", error.getMessage()));
    }

    private boolean isPublicPath(String path) {
        return securityProperties.getPublicPaths().stream().anyMatch(path::startsWith)
                || securityProperties.getDocsPaths().stream().anyMatch(path::contains);
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
    }

    private static class TokenValidationResponse {
        private String emailId;
        private boolean isValid;

        public String getEmailId() {
            return emailId;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}
