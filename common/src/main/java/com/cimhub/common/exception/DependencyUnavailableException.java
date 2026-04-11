package com.cimhub.common.exception;

public class DependencyUnavailableException extends BusinessException {

    public DependencyUnavailableException(String message) {
        super(ErrorCode.DEPENDENCY_UNAVAILABLE, message);
    }
}
