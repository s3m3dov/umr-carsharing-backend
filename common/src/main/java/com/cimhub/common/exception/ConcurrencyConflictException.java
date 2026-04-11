package com.cimhub.common.exception;

/**
 * Exception thrown when a concurrent modification conflict is detected
 * (e.g., optimistic locking failure due to race condition)
 */
public class ConcurrencyConflictException extends BusinessException {

    public ConcurrencyConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
