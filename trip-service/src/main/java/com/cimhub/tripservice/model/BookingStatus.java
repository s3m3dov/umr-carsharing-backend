package com.cimhub.tripservice.model;

public enum BookingStatus {
    REQUESTED, // Passenger requested to join
    CONFIRMED, // Driver confirmed the booking
    IN_PROGRESS, // Trip is currently ongoing
    COMPLETED, // Trip completed successfully
    CANCELLED // Booking was cancelled (by passenger or driver)
}
