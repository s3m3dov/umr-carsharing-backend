package com.cimhub.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestIdGatewayFilterTest {

    private final RequestIdGatewayFilter filter = new RequestIdGatewayFilter();

    @Test
    void filter_whenHeaderMissing_shouldGenerateAndPropagateRequestId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips").build());

        GatewayFilterChain chain = this::assertAndContinue;

        filter.filter(exchange, chain).block();

        String responseRequestId = exchange.getResponse().getHeaders().getFirst(RequestIdGatewayFilter.REQUEST_ID_HEADER);
        assertNotNull(responseRequestId);
    }

    @Test
    void filter_whenHeaderPresent_shouldReuseRequestId() {
        String existingRequestId = "req-123";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/trips")
                        .header(RequestIdGatewayFilter.REQUEST_ID_HEADER, existingRequestId)
                        .build());

        GatewayFilterChain chain = ex -> {
            String propagated = ex.getRequest().getHeaders().getFirst(RequestIdGatewayFilter.REQUEST_ID_HEADER);
            assertEquals(existingRequestId, propagated);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        String responseRequestId = exchange.getResponse().getHeaders().getFirst(RequestIdGatewayFilter.REQUEST_ID_HEADER);
        assertEquals(existingRequestId, responseRequestId);
    }

    private Mono<Void> assertAndContinue(ServerWebExchange exchange) {
        String propagated = exchange.getRequest().getHeaders().getFirst(RequestIdGatewayFilter.REQUEST_ID_HEADER);
        assertNotNull(propagated);
        return Mono.empty();
    }
}
