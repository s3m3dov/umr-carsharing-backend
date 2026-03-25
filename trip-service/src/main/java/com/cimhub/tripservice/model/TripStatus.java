package com.cimhub.tripservice.model;

/**
 * Enum representing the lifecycle states of a Trip
 */
public enum TripStatus {
    CREATED, // Trip created by driver, not yet published/searchable
    AVAILABLE, // Trip published and available for booking (has seats)
    IN_PROGRESS, // Trip is currently ongoing
    COMPLETED, // Trip finished successfully
    CANCELLED // Trip was cancelled
}
