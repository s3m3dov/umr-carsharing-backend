package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when trying to complete a trip that is not in IN_PROGRESS state.
 * Domain exception owned by trip-service.
 */
public class TripNotInProgressException extends BusinessException {

    public TripNotInProgressException(String message) {
        super(TripErrorCode.TRIP_NOT_IN_PROGRESS, message);
    }
}
