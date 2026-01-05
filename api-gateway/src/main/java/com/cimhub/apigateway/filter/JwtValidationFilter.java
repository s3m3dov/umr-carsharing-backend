package com.cimhub.apigateway.filter;

import com.cimhub.apigateway.config.SecurityProperties;
import com.cimhub.apigateway.config.ServiceProperties;
import com.cimhub.apigateway.constant.GatewayConstants;
import com.cimhub.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private final SecurityProperties securityProperties;
    private final ServiceProperties serviceProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        return extractToken(exchange.getRequest(), path)
                .flatMap(this::validateTokenWithAuthService)
                .flatMap(response -> authorize(exchange, chain, response))
                .onErrorMap(this::mapToUnauthorizedIfNeeded);
    }

    /**
     * Attach user info to request if token is valid
     */
    private Mono<Void> authorize(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            TokenValidationResponse response
    ) {
        if (!response.valid()) {
            return Mono.error(new UnauthorizedException("Invalid or expired token"));
        }

        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header(GatewayConstants.USER_EMAIL_HEADER, response.email())
                .header(GatewayConstants.USER_ID_HEADER, response.email().toLowerCase())
                .build();

        return chain.filter(
                exchange.mutate().request(modifiedRequest).build()
        );
    }

    /**
     * Extract and validate Authorization header
     */
    private Mono<String> extractToken(ServerHttpRequest request, String path) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(GatewayConstants.BEARER_PREFIX)) {
            log.warn("Missing Authorization header for path: {}", path);
            return Mono.error(new UnauthorizedException("Missing Authorization header"));
        }

        String token = authHeader
                .substring(GatewayConstants.BEARER_PREFIX.length())
                .trim();

        if (!isValidJwtFormat(token)) {
            log.warn("Malformed JWT token for path: {}", path);
            return Mono.error(new UnauthorizedException("Malformed JWT token"));
        }

        return Mono.just(token);
    }

    /**
     * Validate JWT token with auth-service
     */
    private Mono<TokenValidationResponse> validateTokenWithAuthService(String token) {
        return webClientBuilder.build()
                .post()
                .uri(serviceProperties.getAuth().getValidateUrl())
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class);
    }

    /**
     * Normalize downstream auth errors
     */
    private Throwable mapToUnauthorizedIfNeeded(Throwable error) {
        if (error instanceof UnauthorizedException) {
            return error;
        }

        log.error("Auth-service validation failed", error);
        return new UnauthorizedException("Token validation failed");
    }

    private boolean isPublicPath(String path) {
        return Stream.of(
                        securityProperties.getPublicPaths(),
                        securityProperties.getDocsPaths(),
                        securityProperties.getActuatorPaths()
                )
                .flatMap(Collection::stream)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isValidJwtFormat(String token) {
        if (token == null)
            return false;
        long dotCount = token.chars().filter(ch -> ch == '.').count();
        return dotCount == 2;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /* Inner DTOs */

    private record TokenValidationRequest(String token) {}

    private record TokenValidationResponse(String email, boolean valid) {}
}
