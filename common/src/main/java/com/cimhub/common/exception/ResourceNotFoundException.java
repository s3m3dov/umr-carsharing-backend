package com.cimhub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource is not found
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "RESOURCE_NOT_FOUND";
    }
}
