package com.cimhub.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for business logic errors
 * Extends RuntimeException to avoid checked exception handling
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    // Common business exceptions as static factory methods

    public static BusinessException notFound(String resource, String id) {
        return new BusinessException(
                "RESOURCE_NOT_FOUND",
                String.format("%s with id '%s' not found", resource, id),
                HttpStatus.NOT_FOUND);
    }

    public static BusinessException alreadyExists(String resource, String identifier) {
        return new BusinessException(
                "RESOURCE_ALREADY_EXISTS",
                String.format("%s with %s already exists", resource, identifier),
                HttpStatus.CONFLICT);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(
                "UNAUTHORIZED",
                message,
                HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(
                "FORBIDDEN",
                message,
                HttpStatus.FORBIDDEN);
    }
}
