package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when a reviewer tries to submit a duplicate review.
 * Only one review per reviewer per trip/booking is allowed.
 */
public class ReviewAlreadyExistsException extends BusinessException {

    public ReviewAlreadyExistsException(String message) {
        super(ReviewErrorCode.REVIEW_ALREADY_EXISTS, message);
    }
}
