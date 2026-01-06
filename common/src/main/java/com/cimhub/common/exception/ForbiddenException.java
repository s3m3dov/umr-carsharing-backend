package com.cimhub.common.exception;

/**
 * Exception thrown when a user is forbidden from performing an action
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
