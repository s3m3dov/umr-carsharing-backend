package com.cimhub.tripservice.exception;

public enum TripErrorCode {

    ALREADY_BOOKED,
    ALREADY_CANCELLED,
    INSUFFICIENT_SEATS,
    TRIP_NOT_AVAILABLE;

    public String code() {
        return name();
    }
}
