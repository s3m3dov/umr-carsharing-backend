package com.cimhub.tripservice.exception;

import com.cimhub.common.exception.ErrorType;
import com.cimhub.common.exception.ServiceErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripErrorCode implements ServiceErrorCode {

    ALREADY_BOOKED(HttpStatus.CONFLICT, "User already booked this trip", ErrorType.BUSINESS_RULE_VIOLATION),
    ALREADY_CANCELLED(HttpStatus.CONFLICT, "Trip is already cancelled", ErrorType.BUSINESS_RULE_VIOLATION),
    BOOKING_ALREADY_PROCESSED(HttpStatus.CONFLICT, "Booking has already been processed", ErrorType.BUSINESS_RULE_VIOLATION),
    INSUFFICIENT_SEATS(HttpStatus.CONFLICT, "Not enough seats available", ErrorType.RESOURCE_EXHAUSTED),
    TRIP_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Trip not found or not available", ErrorType.NOT_FOUND),
    TRIP_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "Trip is not in progress", ErrorType.BUSINESS_RULE_VIOLATION),
    RIDE_NOT_COMPLETABLE(HttpStatus.CONFLICT, "Ride cannot be completed in its current state", ErrorType.BUSINESS_RULE_VIOLATION);

    private final HttpStatus httpStatus;
    private final String defaultMessage;
    private final ErrorType defaultType;

    @Override
    public String code() {
        return name();
    }
}
