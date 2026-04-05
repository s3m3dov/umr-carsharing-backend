package com.cimhub.apigateway.filter;

import com.cimhub.apigateway.config.SecurityProperties;
import com.cimhub.apigateway.constant.GatewayConstants;
import com.cimhub.common.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private AdminAuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityProperties securityProperties = new SecurityProperties();
        securityProperties.setAdminPaths(List.of("/api/admin/**"));
        filter = new AdminAuthorizationFilter(securityProperties);
    }

    @Test
    void filter_withNonAdminPath_shouldContinueFilterChain() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips").build());
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_withAdminPathAndAdminRole_shouldContinueFilterChain() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/admin/reviews")
                        .header(GatewayConstants.USER_ROLE_HEADER, UserType.ADMIN.name().toLowerCase(Locale.ROOT))
                        .build());
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_withAdminPathAndNonAdminRole_shouldReturnForbidden() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/admin/reviews")
                        .header(GatewayConstants.USER_ROLE_HEADER, UserType.DRIVER.name())
                        .build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_withAdminPathAndMissingRoleHeader_shouldReturnForbidden() {
        // No X-User-Role header — treated as non-admin
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/admin/users").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_withAdminPathAndPassengerRole_shouldReturnForbidden() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/admin/trips")
                        .header(GatewayConstants.USER_ROLE_HEADER, UserType.PASSENGER.name())
                        .build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }
}
