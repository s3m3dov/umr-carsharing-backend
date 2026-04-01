package com.cimhub.apigateway.filter;

import com.cimhub.apigateway.config.SecurityProperties;
import com.cimhub.apigateway.config.ServiceProperties;
import com.cimhub.apigateway.constant.GatewayConstants;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidationFilterTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    private JwtValidationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.setPublicPaths(List.of("/api/public/**"));
        securityProperties.setDocsPaths(List.of("/v3/api-docs/**"));
        securityProperties.setActuatorPaths(List.of("/actuator/**"));

        ServiceProperties serviceProperties = new ServiceProperties();
        ServiceProperties.AuthConfig authConfig = new ServiceProperties.AuthConfig();
        authConfig.setBaseUrl("http://auth-service");
        authConfig.setValidatePath("/api/auth/validate");
        serviceProperties.setAuth(authConfig);

        filter = new JwtValidationFilter(webClientBuilder, securityProperties, serviceProperties);
    }

    @Test
    void filter_withPublicPath_shouldBypassValidationAndSanitizeIdentityHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/public/ping")
                        .header(GatewayConstants.USER_ID_HEADER, "spoofed-id")
                        .header(GatewayConstants.USER_EMAIL_HEADER, "spoofed@example.com")
                        .header(GatewayConstants.USER_ROLE_HEADER, UserType.ADMIN.name())
                        .build());

        GatewayFilterChain chain = mutatedExchange -> {
            assertNull(mutatedExchange.getRequest().getHeaders().getFirst(GatewayConstants.USER_ID_HEADER));
            assertNull(mutatedExchange.getRequest().getHeaders().getFirst(GatewayConstants.USER_EMAIL_HEADER));
            assertNull(mutatedExchange.getRequest().getHeaders().getFirst(GatewayConstants.USER_ROLE_HEADER));
            return reactor.core.publisher.Mono.empty();
        };

        filter.filter(exchange, chain).block();
    }

    @Test
    void filter_withMissingAuthorizationHeader_shouldThrowUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips").build());

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> filter.filter(exchange, unused -> reactor.core.publisher.Mono.empty()).block());

        assertEquals("Missing Authorization header", ex.getMessage());
    }

    @Test
    void filter_withMalformedToken_shouldThrowUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips")
                        .header(HttpHeaders.AUTHORIZATION, GatewayConstants.BEARER_PREFIX + "malformed-token")
                        .build());

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> filter.filter(exchange, unused -> Mono.empty()).block());

        assertEquals("Malformed JWT token", ex.getMessage());
    }

    @Test
    void filter_whenAuthServiceFails_shouldMapToUnauthorized() {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(anyString())).thenReturn(bodyUriSpec);
        when(bodyUriSpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.error(new RuntimeException("auth unavailable")));

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips")
                        .header(HttpHeaders.AUTHORIZATION, GatewayConstants.BEARER_PREFIX + "a.b.c")
                        .build());

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> filter.filter(exchange, unused -> Mono.empty()).block());

        assertEquals("Token validation failed", ex.getMessage());
    }
}
