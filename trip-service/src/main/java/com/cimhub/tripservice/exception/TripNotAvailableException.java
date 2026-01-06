package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a trip is not available for booking.
 * Domain exception owned by trip-service.
 */
public class TripNotAvailableException extends BusinessException {

    public TripNotAvailableException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return TripErrorCode.TRIP_NOT_AVAILABLE.code();
    }
}
