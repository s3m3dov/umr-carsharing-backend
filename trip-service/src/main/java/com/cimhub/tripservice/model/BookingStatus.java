package com.cimhub.tripservice.model;

public enum BookingStatus {
    REQUESTED, // Passenger requested to join
    REJECTED, // Driver rejected the booking request
    CONFIRMED, // Driver confirmed the booking
    IN_PROGRESS, // Trip is currently ongoing
    COMPLETED, // Trip completed successfully
    CANCELLED // Booking was cancelled (by passenger or driver)
}
