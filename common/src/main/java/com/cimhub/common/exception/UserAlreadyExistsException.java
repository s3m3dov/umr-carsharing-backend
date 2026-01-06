package com.cimhub.common.exception;

/**
 * Exception thrown when a resource (e.g., user, profile) already exists
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message);
    }
}
