package com.cimhub.common.exception;

/**
 * Exception thrown when a user is forbidden from performing an action
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        this(message, null);
    }

    public ForbiddenException(String message, String requestPath) {
        super(ErrorCode.FORBIDDEN, message, requestPath);
    }
}
