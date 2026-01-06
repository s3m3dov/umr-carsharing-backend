package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when trying to cancel an already cancelled booking or trip.
 * Domain exception owned by trip-service.
 */
public class AlreadyCancelledException extends BusinessException {

    public AlreadyCancelledException(String message) {
        super(TripErrorCode.ALREADY_CANCELLED, message, null);
    }
}
