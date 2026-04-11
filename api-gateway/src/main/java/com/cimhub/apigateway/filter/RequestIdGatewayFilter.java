package com.cimhub.apigateway.filter;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        String finalRequestId = requestId;
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(REQUEST_ID_HEADER, finalRequestId)
                .build();

        ServerWebExchange mutatedExchange =
                exchange.mutate().request(mutatedRequest).build();

        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, finalRequestId);

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            // Ensure header is present in response (might be redundant if added above but
            // safe)
            if (!exchange.getResponse().getHeaders().containsKey(REQUEST_ID_HEADER)) {
                exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, finalRequestId);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
