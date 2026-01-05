package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource (e.g., user, profile) already exists
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "USER_ALREADY_EXISTS";
    }
}
