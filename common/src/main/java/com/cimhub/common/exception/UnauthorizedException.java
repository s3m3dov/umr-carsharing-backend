package com.cimhub.common.exception;

/**
 * Exception thrown for unauthorized access or invalid credentials
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        this(message, null);
    }

    public UnauthorizedException(String message, String requestPath) {
        super(ErrorCode.UNAUTHORIZED, message, requestPath);
    }
}
