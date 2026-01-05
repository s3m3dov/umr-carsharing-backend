package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for unauthorized access or invalid credentials
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
