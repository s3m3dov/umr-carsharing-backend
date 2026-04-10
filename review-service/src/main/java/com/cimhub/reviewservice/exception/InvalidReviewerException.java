package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

public class InvalidReviewerException extends BusinessException {

    public InvalidReviewerException(String message) {
        super(ReviewErrorCode.INVALID_REVIEWER, message);
    }
}
