package com.cimhub.reviewservice.exception;

import com.cimhub.common.exception.ErrorType;
import com.cimhub.common.exception.ServiceErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ServiceErrorCode {

    TRIP_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "Trip must be completed before review can be submitted", ErrorType.BUSINESS_RULE_VIOLATION),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "Review already exists for this reviewer and trip/booking", ErrorType.BUSINESS_RULE_VIOLATION),
    UNAUTHORIZED_REVIEWER(HttpStatus.FORBIDDEN, "Reviewer is not part of this trip", ErrorType.AUTH_ERROR),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5", ErrorType.VALIDATION_ERROR);

    private final HttpStatus httpStatus;
    private final String defaultMessage;
    private final ErrorType defaultType;

    @Override
    public String code() {
        return name();
    }
}
