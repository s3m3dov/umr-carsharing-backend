package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when attempting to review a trip that is not yet completed.
 */
public class TripNotCompletedException extends BusinessException {

    public TripNotCompletedException(String message) {
        super(ReviewErrorCode.TRIP_NOT_COMPLETED, message);
    }
}
