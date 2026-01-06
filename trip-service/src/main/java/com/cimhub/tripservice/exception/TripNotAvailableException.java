package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when a trip is not available for booking.
 * Domain exception owned by trip-service.
 */
public class TripNotAvailableException extends BusinessException {

    public TripNotAvailableException(String message) {
        super(TripErrorCode.TRIP_NOT_AVAILABLE, message);
    }
}
