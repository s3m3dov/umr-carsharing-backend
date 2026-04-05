package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.dto.common.LocationPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindRideRequestTest {

    @Test
    void builder_shouldPopulateAllFields() {
        LocationPoint pickup = new LocationPoint(52.5, 13.4, "Pickup");
        LocationPoint destination = new LocationPoint(52.6, 13.5, "Destination");

        FindRideRequest request = FindRideRequest.builder()
                .pickupPoint(pickup)
                .destinationPoint(destination)
                .rideStartTime("2026-08-01T10:00:00Z")
                .requestedSeats(2)
                .build();

        assertEquals(pickup, request.pickupPoint());
        assertEquals(destination, request.destinationPoint());
        assertEquals("2026-08-01T10:00:00Z", request.rideStartTime());
        assertEquals(2, request.requestedSeats());
    }
}
