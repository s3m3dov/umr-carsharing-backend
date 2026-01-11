package com.cimhub.tripservice.model;

/**
 * Enum representing the lifecycle states of a Trip
 */
public enum TripStatus {
    OFFERED, // Trip created, waiting for bookings
    AVAILABLE, // Trip available for booking (has seats)
    IN_PROGRESS, // Trip is currently ongoing
    COMPLETED, // Trip finished successfully
    CANCELLED // Trip was cancelled
}
