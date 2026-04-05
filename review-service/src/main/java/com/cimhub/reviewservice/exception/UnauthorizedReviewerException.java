package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

/**
 * Exception thrown when a user who is not part of the trip attempts to submit a review.
 */
public class UnauthorizedReviewerException extends BusinessException {

    public UnauthorizedReviewerException(String message) {
        super(ReviewErrorCode.UNAUTHORIZED_REVIEWER, message);
    }
}
