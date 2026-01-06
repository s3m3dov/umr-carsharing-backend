package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when a user has already booked a trip.
 * Domain exception owned by trip-service.
 */
public class AlreadyBookedException extends BusinessException {

    public AlreadyBookedException(String message) {
        super(TripErrorCode.ALREADY_BOOKED, message);
    }
}
