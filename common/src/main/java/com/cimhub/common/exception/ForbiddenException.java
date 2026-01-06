package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is forbidden from performing an action
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.FORBIDDEN.code();
    }
}
