package com.cimhub.apigateway.exception;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ErrorBody;
import com.cimhub.common.exception.BusinessException;
import com.cimhub.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for API Gateway.
 * Gateway-level errors: create standardized ApiError.
 * Downstream errors: forward raw response body.
 */
@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        // Connection errors to downstream services
        if (ex instanceof WebClientRequestException) {
            return handleServiceUnavailable(exchange, ex);
        }

        // Downstream service errors - forward response as-is
        if (ex instanceof WebClientResponseException webClientEx) {
            return forwardDownstreamError(exchange, webClientEx);
        }

        // Gateway-level errors - create standardized response
        return handleGatewayError(exchange, ex);
    }

    private Mono<Void> handleServiceUnavailable(ServerWebExchange exchange, Throwable ex) {
        log.error("Downstream service unavailable", ex);
        return handleGatewayError(exchange, ex);
    }

    /**
     * Forward downstream service error response without modification
     */
    private Mono<Void> forwardDownstreamError(ServerWebExchange exchange, WebClientResponseException ex) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(ex.getStatusCode());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";
        String path = exchange.getRequest().getURI().getPath();

        if (ex.getStatusCode().is5xxServerError()) {
            log.error("Downstream error [{} {}]: {}", method, path, ex.getStatusCode(), ex);
        } else {
            log.warn("Downstream error [{} {}]: {}", method, path, ex.getStatusCode());
        }

        byte[] body = ex.getResponseBodyAsByteArray();
        if (body.length == 0) {
            // No body from downstream - create minimal error
            body = String.format(
                            "{\"status\":%d,\"error\":\"%s\"}",
                            ex.getStatusCode().value(), ex.getStatusText())
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Handle gateway-level errors (auth, routing, connection, etc.)
     */
    private Mono<Void> handleGatewayError(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String code;
        String message;

        if (ex instanceof BusinessException businessEx) {
            status = businessEx.getErrorCode().getHttpStatus();
            code = businessEx.getErrorCode().code();
            message = businessEx.getMessage();
        } else if (ex instanceof NoResourceFoundException notFoundEx) {
            status = HttpStatus.NOT_FOUND;
            code = ErrorCode.RESOURCE_NOT_FOUND.code();
            message = notFoundEx.getReason();
        } else if (isConnectionError(ex)) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            code = HttpStatus.SERVICE_UNAVAILABLE.name();
            message = "Upstream service unavailable";
        } else if (ex instanceof IllegalArgumentException illegalArgEx) {
            status = HttpStatus.BAD_REQUEST;
            code = ErrorCode.BAD_REQUEST.code();
            message = illegalArgEx.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = ErrorCode.INTERNAL_SERVER_ERROR.code();
            message = "Gateway error";
        }

        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";
        String path = exchange.getRequest().getURI().getPath();

        if (status.is5xxServerError()) {
            log.error("Gateway error [{} {}]", method, path, ex);
        } else {
            log.warn("Gateway error [{} {}]: {}", method, path, message);
        }

        return writeGatewayError(exchange, status, code, message);
    }

    private boolean isConnectionError(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof ConnectException) {
                return true;
            }
            String className = current.getClass().getName();
            if (className.contains("ConnectException") || className.contains("ConnectTimeoutException")) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> writeGatewayError(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        String path = exchange.getRequest().getURI().getPath();

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .error(ErrorBody.of(path, Collections.emptyList()))
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();

        ApiResponseWrapper<Object> wrapper = ApiResponseWrapper.error(apiError);

        try {
            byte[] body = objectMapper.writeValueAsBytes(wrapper);
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ApiError", e);
            return response.setComplete();
        }
    }
}
