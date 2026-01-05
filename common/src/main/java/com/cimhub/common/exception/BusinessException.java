package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for business logic errors
 * Extends RuntimeException to avoid checked exception handling
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
     * @return The machine-readable error code associated with this business error
     */
    public abstract ErrorCode getErrorCode();
}
