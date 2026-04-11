package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

public class DriverNotFoundException extends BusinessException {

    public DriverNotFoundException(String message) {
        super(ReviewErrorCode.DRIVER_NOT_FOUND, message);
    }
}
