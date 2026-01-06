package com.cimhub.common.exception;

/**
 * Global error codes for cross-service errors.
 * These codes are part of the public API contract and used by all services.
 */
public enum ErrorCode {
    // Cross-Service
    USER_ALREADY_EXISTS,

    // Shared
    RESOURCE_NOT_FOUND,

    // Security
    UNAUTHORIZED,
    FORBIDDEN,

    // Validation / request
    BAD_REQUEST,
    VALIDATION_ERROR,

    // Generic
    INTERNAL_SERVER_ERROR;

    /**
     * String representation exposed to clients.
     */
    public String code() {
        return name();
    }
}
