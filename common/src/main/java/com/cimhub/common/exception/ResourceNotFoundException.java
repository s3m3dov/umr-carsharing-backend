package com.cimhub.common.exception;

/**
 * Exception thrown when a resource is not found
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        this(message, null);
    }

    public ResourceNotFoundException(String message, String requestPath) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, requestPath);
    }
}
