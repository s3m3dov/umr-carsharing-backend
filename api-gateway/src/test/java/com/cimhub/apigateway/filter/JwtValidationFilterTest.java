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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
