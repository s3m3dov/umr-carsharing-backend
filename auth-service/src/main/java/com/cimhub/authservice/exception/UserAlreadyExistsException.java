package com.cimhub.authservice.exception;

import com.cimhub.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user already exists during registration
 */
public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message, HttpStatus.CONFLICT);
    }
}
