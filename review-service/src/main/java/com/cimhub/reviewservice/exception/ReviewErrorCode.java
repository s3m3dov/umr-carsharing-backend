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
    INVALID_RATING(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5", ErrorType.VALIDATION_ERROR),
    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Driver not found", ErrorType.NOT_FOUND),
    PASSENGER_NOT_FOUND(HttpStatus.NOT_FOUND, "Passenger not found", ErrorType.NOT_FOUND),
    REVIEwee_NOT_FOUND(HttpStatus.NOT_FOUND, "Reviewee not found", ErrorType.NOT_FOUND),
    INVALID_REVIEWER(HttpStatus.BAD_REQUEST, "Invalid reviewer: user must be a passenger to review a driver", ErrorType.VALIDATION_ERROR),
    INVALID_REVIEW_TARGET(HttpStatus.BAD_REQUEST, "Invalid review target: cannot review a passenger from driver endpoint or vice versa", ErrorType.VALIDATION_ERROR),
    NO_REVIEWS_FOUND(HttpStatus.NOT_FOUND, "No reviews found", ErrorType.NOT_FOUND);

    private final HttpStatus httpStatus;
    private final String defaultMessage;
    private final ErrorType defaultType;

    @Override
    public String code() {
        return name();
    }
}
