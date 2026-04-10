package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

public class InvalidRatingException extends BusinessException {

    public InvalidRatingException(String message) {
        super(ReviewErrorCode.INVALID_RATING, message);
    }
}
