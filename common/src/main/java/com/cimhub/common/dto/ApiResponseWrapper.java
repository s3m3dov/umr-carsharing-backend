package com.cimhub.common.dto;

import com.cimhub.common.dto.response.ApiError;
import com.cimhub.common.dto.response.ErrorBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Unified API response wrapper for all endpoints
 * Either 'data' (success) or 'error' (failure) will be present, never both
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWrapper<T> {

    /**
     * Response data - present on success
     */
    private T data;

    /**
     * Error information - present on failure
     */
    private ApiError error;

    /**
     * General message - present optionally
     */
    private String message;

    /**
     * Response timestamp
     */
    private Instant timestamp;

    // ==================== Factory Methods ====================

    /**
     * Create a successful response with data
     */
    public static <T> ApiResponseWrapper<T> success(T data) {
        return ApiResponseWrapper.<T>builder()
                .data(data)
                .message("Operation successful")
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful response with message
     */
    public static <T> ApiResponseWrapper<T> success(String message) {
        return ApiResponseWrapper.<T>builder()
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful response with data and message
     */
    public static <T> ApiResponseWrapper<T> success(T data, String message) {
        return ApiResponseWrapper.<T>builder()
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with structured error
     */
    public static <T> ApiResponseWrapper<T> error(ApiError error) {
        return ApiResponseWrapper.<T>builder()
                .error(error)
                .message("An error occurred")
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with code and message
     */
    public static <T> ApiResponseWrapper<T> error(String code, String message) {
        return error(ApiError.builder()
                .code(code)
                .message(message)
                .build());
    }

    /**
     * Create an error response with code, message, and path
     */
    public static <T> ApiResponseWrapper<T> error(String code, String message, String path) {
        return error(ApiError.builder()
                .code(code)
                .message(message)
                .error(ErrorBody.of(path))
                .build());
    }
}
