package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to cancel an already cancelled booking or trip.
 * Domain exception owned by trip-service.
 */
public class AlreadyCancelledException extends BusinessException {

    public AlreadyCancelledException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return TripErrorCode.ALREADY_CANCELLED.code();
    }
}
