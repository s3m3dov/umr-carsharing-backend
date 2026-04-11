package com.cimhub.common.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API response wrapper for successful endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccess<T> {

    /**
     * Response data
     */
    private T data;

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
    public static <T> ApiSuccess<T> success(T data) {
        return ApiSuccess.<T>builder()
                .data(data)
                .message("Operation successful")
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful response with message
     */
    public static <T> ApiSuccess<T> success(String message) {
        return ApiSuccess.<T>builder()
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful response with data and message
     */
    public static <T> ApiSuccess<T> success(T data, String message) {
        return ApiSuccess.<T>builder()
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
