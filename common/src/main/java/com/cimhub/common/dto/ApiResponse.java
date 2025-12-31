package com.cimhub.common.dto;

import com.cimhub.common.dto.response.ApiError;
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
public class ApiResponse<T> {

    /**
     * Response data - present on success
     */
    private T data;

    /**
     * Error information - present on failure
     */
    private ApiError error;

    /**
     * Response timestamp
     */
    private Instant timestamp;

    // ==================== Factory Methods ====================

    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with structured error
     */
    public static <T> ApiResponse<T> error(ApiError error) {
        return ApiResponse.<T>builder()
                .error(error)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with code and message
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return error(ApiError.builder()
                .code(code)
                .message(message)
                .build());
    }

    /**
     * Create an error response with code, message, and path
     */
    public static <T> ApiResponse<T> error(String code, String message, String path) {
        return error(ApiError.builder()
                .code(code)
                .message(message)
                .path(path)
                .build());
    }
}
