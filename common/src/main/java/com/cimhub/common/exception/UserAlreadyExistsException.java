package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource (e.g., user, profile) already exists
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message, HttpStatus.CONFLICT);
    }
}
