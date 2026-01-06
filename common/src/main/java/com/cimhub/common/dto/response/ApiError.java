package com.cimhub.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Standardized error response contract.
 * All error responses across the system use this format.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String code,
        String message,
        String path,
        List<Object> details,
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
                .path(path)
                .details(Collections.emptyList())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error with structured details.
     */
    public static ApiError withDetails(int status, String code, String message, String path,
            List<Object> details) {
        return ApiError.builder()
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .details(details)
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
                .path(path)
                .details(Collections.emptyList())
                .timestamp(Instant.now())
                .requestId(requestId)
                .build();
    }
}
