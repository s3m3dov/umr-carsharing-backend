package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Interface for error codes to allow service-specific enums.
 */
public interface ServiceErrorCode {
    HttpStatus getHttpStatus();

    String getDefaultMessage();

    ErrorType getDefaultType();

    String code();
}
