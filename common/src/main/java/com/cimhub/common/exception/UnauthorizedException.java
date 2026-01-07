package com.cimhub.common.exception;

/**
 * Exception thrown for unauthorized access or invalid credentials
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
