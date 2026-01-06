package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when there are insufficient seats available.
 * Domain exception owned by trip-service.
 */
public class InsufficientSeatsException extends BusinessException {

    public InsufficientSeatsException(String message) {
        super(TripErrorCode.INSUFFICIENT_SEATS, message);
    }
}
