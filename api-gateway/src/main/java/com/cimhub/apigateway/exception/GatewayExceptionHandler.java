package com.cimhub.apigateway.exception;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.exception.BusinessException;
import com.cimhub.common.exception.ErrorCode;
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

/**
 * Global exception handler for API Gateway.
 * Converts exceptions thrown from Gateway filters into standardized ApiError
 * responses.
 */
@Slf4j
@Component
@Order(-2) // High priority to catch exceptions before default Spring handlers
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error occurred: {}", ex.getMessage(), ex);

        HttpStatus status;
        ErrorCode errorCode;
        String message = ex.getMessage();

        if (ex instanceof BusinessException businessEx) {
            status = businessEx.getHttpStatus();
            errorCode = businessEx.getErrorCode();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred at the gateway level.";
        }

        return writeErrorResponse(exchange, status, errorCode, message);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, ErrorCode errorCode,
            String message) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(new RuntimeException("Response already committed"));
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiError apiError = ApiError.builder()
                .code(errorCode.name())
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(apiError);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ApiError to JSON", e);
            return response.setComplete();
        }
    }
}
