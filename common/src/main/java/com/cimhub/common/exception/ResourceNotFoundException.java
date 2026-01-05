package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource is not found
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
