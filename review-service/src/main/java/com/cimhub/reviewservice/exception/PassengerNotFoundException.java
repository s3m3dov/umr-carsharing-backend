package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

public class PassengerNotFoundException extends BusinessException {

    public PassengerNotFoundException(String message) {
        super(ReviewErrorCode.PASSENGER_NOT_FOUND, message);
    }
}
