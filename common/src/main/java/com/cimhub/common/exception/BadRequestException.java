package com.cimhub.common.exception;

/**
 * Exception thrown when request input is invalid for business rules.
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
