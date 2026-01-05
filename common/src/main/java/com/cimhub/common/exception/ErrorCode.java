package com.cimhub.common.exception;

/**
 * Standardized error codes for the entire system.
 * These codes are part of the public API contract.
 */
public enum ErrorCode {

    // Business errors
    RESOURCE_NOT_FOUND,
    USER_ALREADY_EXISTS,

    // Auth / security
    UNAUTHORIZED,

    // Validation / request
    BAD_REQUEST,
    VALIDATION_ERROR,

    // Technical
    INTERNAL_SERVER_ERROR;

    /**
     * String representation exposed to clients.
     */
    public String code() {
        return name();
    }
}
