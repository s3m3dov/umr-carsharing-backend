package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user has already booked a trip.
 * Domain exception owned by trip-service.
 */
public class AlreadyBookedException extends BusinessException {

    public AlreadyBookedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return TripErrorCode.ALREADY_BOOKED.code();
    }
}
