package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there are insufficient seats available.
 * Domain exception owned by trip-service.
 */
public class InsufficientSeatsException extends BusinessException {

    public InsufficientSeatsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return TripErrorCode.INSUFFICIENT_SEATS.code();
    }
}
