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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
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
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        ErrorAttributes attrs = resolveErrorAttributes(ex);
        logException(exchange, ex, attrs);
        return writeErrorResponse(exchange, attrs);
    }

    private ErrorAttributes resolveErrorAttributes(Throwable ex) {
        if (ex instanceof BusinessException businessEx) {
            return new ErrorAttributes(
                    businessEx.getErrorCode().getHttpStatus(),
                    businessEx.getErrorCode(),
                    businessEx.getMessage());
        }

        if (ex instanceof NoResourceFoundException noResourceFoundException) {
            return new ErrorAttributes(
                    HttpStatus.NOT_FOUND,
                    ErrorCode.RESOURCE_NOT_FOUND,
                    noResourceFoundException.getReason());
        }

        if (ex instanceof ErrorResponseException errorResponseException) {
            HttpStatus status = toHttpStatus(errorResponseException.getStatusCode());
            return new ErrorAttributes(
                    status,
                    toErrorCode(status),
                    errorResponseException.getBody().getDetail());
        }

        return new ErrorAttributes(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred at the gateway level.");
    }

    private void logException(ServerWebExchange exchange, Throwable ex, ErrorAttributes attrs) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";

        if (attrs.status().is5xxServerError()) {
            log.error("Gateway exception [{} {}]", method, path, ex);
            return;
        }

        log.warn("Gateway request error [{} {}]: {}", method, path, attrs.message());
    }

    private HttpStatus toHttpStatus(HttpStatusCode statusCode) {
        HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ErrorCode toErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND;
            case UNPROCESSABLE_ENTITY -> ErrorCode.VALIDATION_ERROR;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
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
