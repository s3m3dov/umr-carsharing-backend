package com.cimhub.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Structured error response
 * Provides machine-readable error codes and human-readable messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    /**
     * Machine-readable error code (e.g., USER_ALREADY_EXISTS, VALIDATION_ERROR)
     * Can be used for i18n on frontend or programmatic error handling
     */
    private String code;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Request path where error occurred (e.g., /api/trips/offer)
     * Useful for debugging and error tracking
     */
    private String path;

    /**
     * Additional error details (e.g., validation errors for multiple fields)
     */
    private List<String> details;
}
