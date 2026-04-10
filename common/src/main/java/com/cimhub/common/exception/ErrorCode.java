package com.cimhub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Global error codes for cross-service errors.
 * Each code includes HTTP status, default message, and error type.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ServiceErrorCode {
    // Cross-Service
    USER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "User already exists",
            ErrorType.DUPLICATE_RESOURCE),

    // Shared
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Resource not found",
            ErrorType.NOT_FOUND),

    // Security
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "Authentication required",
            ErrorType.AUTH_ERROR),
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "Access denied",
            ErrorType.AUTH_ERROR),

    // Validation / request
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "Invalid request",
            ErrorType.VALIDATION_ERROR),
    VALIDATION_ERROR(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Validation failed",
            ErrorType.VALIDATION_ERROR),
    DEPENDENCY_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Required upstream service is unavailable",
            ErrorType.SYSTEM_ERROR),

    // Concurrency
    CONFLICT(
            HttpStatus.CONFLICT,
            "Resource conflict - please retry",
            ErrorType.CONFLICT_ERROR),

    // Generic
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            ErrorType.SYSTEM_ERROR);

    private final HttpStatus httpStatus;
    private final String defaultMessage;
    private final ErrorType defaultType;

    /**
     * String representation exposed to clients.
     */
    public String code() {
        return name();
    }
}
