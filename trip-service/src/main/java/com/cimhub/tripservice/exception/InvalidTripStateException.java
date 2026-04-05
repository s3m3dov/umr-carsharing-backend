package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when a trip action is invalid for the current trip status.
 */
public class InvalidTripStateException extends BusinessException {

    public InvalidTripStateException(String message) {
        super(TripErrorCode.INVALID_TRIP_STATE, message);
    }
}
