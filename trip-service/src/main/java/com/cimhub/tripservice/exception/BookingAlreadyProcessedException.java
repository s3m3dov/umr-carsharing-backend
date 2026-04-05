package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when a driver tries to accept or reject a booking
 * that is no longer in REQUESTED state.
 * Domain exception owned by trip-service.
 */
public class BookingAlreadyProcessedException extends BusinessException {

    public BookingAlreadyProcessedException(String message) {
        super(TripErrorCode.BOOKING_ALREADY_PROCESSED, message);
    }
}
