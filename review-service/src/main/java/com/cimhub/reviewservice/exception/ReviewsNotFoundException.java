package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.BusinessException;

public class ReviewsNotFoundException extends BusinessException {

    public ReviewsNotFoundException(String message) {
        super(ReviewErrorCode.NO_REVIEWS_FOUND, message);
    }
}
