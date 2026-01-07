package com.cimhub.apigateway.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ErrorBody;
import com.cimhub.common.exception.BusinessException;
import com.cimhub.common.exception.ErrorCode;
import com.cimhub.common.exception.ServiceErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

/**
 * Global exception handler for API Gateway.
 * Converts gateway-level exceptions into standardized ApiError responses.
 */
@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway exception", ex);

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        ErrorAttributes attrs = resolveErrorAttributes(ex);
        return writeErrorResponse(exchange, attrs);
    }

    private ErrorAttributes resolveErrorAttributes(Throwable ex) {
        if (ex instanceof BusinessException businessEx) {
            return new ErrorAttributes(
                    businessEx.getErrorCode().getHttpStatus(),
                    businessEx.getErrorCode(),
                    businessEx.getMessage());
        }

        return new ErrorAttributes(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred at the gateway level.");
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorAttributes attrs) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(attrs.status());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        String path = exchange.getRequest().getURI().getPath();

        ApiError apiError = ApiError.builder()
                .status(attrs.status().value())
                .code(attrs.errorCode().code())
                .message(attrs.message())
                .error(ErrorBody.of(path, Collections.emptyList()))
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();

        try {
            byte[] body = objectMapper.writeValueAsBytes(apiError);
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ApiError", e);
            return response.setComplete();
        }
    }

    /**
     * Internal error descriptor
     */
    private record ErrorAttributes(
            HttpStatus status,
            ServiceErrorCode errorCode,
            String message) {
    }
}
