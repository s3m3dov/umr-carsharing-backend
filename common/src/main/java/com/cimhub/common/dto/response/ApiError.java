package com.cimhub.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response contract.
 * All error responses across the system use this format.
 * Root fields:
 * - status: HTTP status code (single source of truth)
 * - code: Application/business code (always present)
 * - message: Human-readable summary
 * - error: Present for ERROR responses (contains path and details)
 * - timestamp: ISO-8601 server time
 * - requestId: Optional request correlation ID
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String code,
        String message,
        ErrorBody error,
        Instant timestamp,
        String requestId) {

    /**
     * Create a simple error without details.
     */
    public static ApiError of(int status, String code, String message, String path) {
        return ApiError.builder()
                .status(status)
                .code(code)
                .message(message)
                .error(ErrorBody.of(path))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error with structured details.
     */
    public static ApiError withDetails(int status, String code, String message, String path,
            List<ErrorDetail> details) {
        return ApiError.builder()
                .status(status)
                .code(code)
                .message(message)
                .error(ErrorBody.of(path, details))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error with request ID for tracing.
     */
    public static ApiError withRequestId(int status, String code, String message, String path, String requestId) {
        return ApiError.builder()
                .status(status)
                .code(code)
                .message(message)
                .error(ErrorBody.of(path))
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }

    /**
     * Create an error with request ID and details for tracing.
     */
    public static ApiError withRequestIdAndDetails(int status, String code, String message, String path,
            List<ErrorDetail> details, String requestId) {
        return ApiError.builder()
                .status(status)
                .code(code)
                .message(message)
                .error(ErrorBody.of(path, details))
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }
}
