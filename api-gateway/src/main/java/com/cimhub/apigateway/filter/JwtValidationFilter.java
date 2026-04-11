package com.cimhub.apigateway.filter;

import com.cimhub.apigateway.config.SecurityProperties;
import com.cimhub.apigateway.config.ServiceProperties;
import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.enums.RequestHeaderName;
import com.cimhub.common.exception.DependencyUnavailableException;
import com.cimhub.common.exception.UnauthorizedException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
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

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Strip any incoming X-User-* headers to prevent identity spoofing
        ServerHttpRequest sanitizedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    for (RequestHeaderName header : RequestHeaderName.values()) {
                        headers.remove(header.getHeaderName());
                    }
                })
                .build();
        ServerWebExchange sanitizedExchange =
                exchange.mutate().request(sanitizedRequest).build();

        if (HttpMethod.OPTIONS.equals(sanitizedExchange.getRequest().getMethod())) {
            return chain.filter(sanitizedExchange);
        }

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(sanitizedExchange);
        }

        return extractToken(sanitizedExchange.getRequest(), path)
                .flatMap(this::validateTokenWithAuthService)
                .flatMap(response -> authorize(sanitizedExchange, chain, response))
                .onErrorMap(this::mapToUnauthorizedIfNeeded);
    }

    /**
     * Attach user info to request if token is valid
     */
    private Mono<Void> authorize(
            ServerWebExchange exchange, GatewayFilterChain chain, TokenValidationResponse response) {
        if (!response.valid() || response.email() == null || response.role() == null) {
            log.warn(
                    "Token validation failed or missing user info: valid={}, email={}, role={}",
                    response.valid(),
                    response.email(),
                    response.role());
            return Mono.error(new UnauthorizedException("Invalid or expired token, or missing user info"));
        }

        String email = response.email().toLowerCase();
        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header(RequestHeaderName.USER_EMAIL.getHeaderName(), email)
                .header(RequestHeaderName.USER_ID.getHeaderName(), email)
                .header(RequestHeaderName.USER_ROLE.getHeaderName(), response.role())
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Extract and validate Authorization header
     */
    private Mono<String> extractToken(ServerHttpRequest request, String path) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(HeaderConstants.BEARER_PREFIX)) {
            log.warn("Missing Authorization header for path: {}", path);
            return Mono.error(new UnauthorizedException("Missing Authorization header"));
        }

        String token =
                authHeader.substring(HeaderConstants.BEARER_PREFIX.length()).trim();

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
        return webClientBuilder
                .build()
                .post()
                .uri(serviceProperties.getAuth().getValidateUrl())
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(AuthServiceResponse.class)
                .map(AuthServiceResponse::data);
    }

    /**
     * Normalize downstream auth errors
     */
    private Throwable mapToUnauthorizedIfNeeded(Throwable error) {
        if (error instanceof UnauthorizedException) {
            return error;
        }

        if (isConnectionError(error)) {
            log.error("Auth-service unavailable", error);
            return new DependencyUnavailableException("Auth-service unavailable");
        }

        log.error("Auth-service validation failed", error);
        return new UnauthorizedException("Token validation failed");
    }

    private boolean isConnectionError(Throwable error) {
        String errorClass = error.getClass().getName();
        return errorClass.contains("ConnectException")
                || errorClass.contains("ReadTimeoutException")
                || errorClass.contains("GatewayTimeoutException")
                || (errorClass.contains("WebClientResponseException")
                        && error.getMessage() != null
                        && error.getMessage().contains("Connection"));
    }

    private boolean isPublicPath(String path) {
        return Stream.of(
                        securityProperties.getPublicPaths(),
                        securityProperties.getDocsPaths(),
                        securityProperties.getActuatorPaths())
                .flatMap(Collection::stream)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isValidJwtFormat(String token) {
        if (token == null) return false;
        long dotCount = token.chars().filter(ch -> ch == '.').count();
        return dotCount == 2;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /* Inner DTOs */

    private record TokenValidationRequest(String token) {}

    private record AuthServiceResponse(TokenValidationResponse data) {}

    private record TokenValidationResponse(
            @JsonProperty("isValid") boolean isValid, String email, String role) {

        public boolean valid() {
            return isValid;
        }
    }
}
