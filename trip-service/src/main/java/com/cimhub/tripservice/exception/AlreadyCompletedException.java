package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

public class AlreadyCompletedException extends BusinessException {

    public AlreadyCompletedException(String message) {
        super(TripErrorCode.ALREADY_COMPLETED, message);
    }
}
