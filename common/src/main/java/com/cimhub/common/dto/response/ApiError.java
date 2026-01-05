package com.cimhub.common.dto.response;

import lombok.Builder;
import java.util.List;
import java.util.Collections;

/**
 * Structured error response
 * Provides machine-readable error codes and human-readable messages
 */
@Builder
public record ApiError(
        String code,
        String message,
        String path,
        List<String> details) {
    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, path, Collections.emptyList());
    }

    public static ApiError withDetails(String code, String message, String path, List<String> details) {
        return new ApiError(code, message, path, details);
    }
}
