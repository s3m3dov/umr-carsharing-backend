package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when trying to complete a ride (booking) that is not in
 * a completable state (CONFIRMED or IN_PROGRESS).
 * Domain exception owned by trip-service.
 */
public class RideNotCompletableException extends BusinessException {

    public RideNotCompletableException(String message) {
        super(TripErrorCode.RIDE_NOT_COMPLETABLE, message);
    }
}
