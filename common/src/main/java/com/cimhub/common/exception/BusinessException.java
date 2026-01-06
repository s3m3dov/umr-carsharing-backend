package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for business logic errors.
 * Extends RuntimeException to avoid checked exception handling.
 *
 * Subclasses must provide:
 * HTTP status code
 * Error code string (from ErrorCode enum or domain-specific enum)
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }

    /**
     * @return The HTTP status code associated with this business error
     */
    public abstract HttpStatus getHttpStatus();

    /**
     * @return The machine-readable error code string associated with this business
     *         error.
     *         This can come from the global ErrorCode enum or a domain-specific
     *         enum.
     */
    public abstract String getErrorCode();
}
