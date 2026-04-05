package com.cimhub.tripservice.dto.request;

import com.cimhub.common.models.Points;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindRideRequestTest {

    @Test
    void builder_shouldPopulateAllFields() {
        Points pickup = new Points();
        pickup.setLatitude(52.5);
        pickup.setLongitude(13.4);
        pickup.setPlaceAddress("Pickup");
        Points destination = new Points();
        destination.setLatitude(52.6);
        destination.setLongitude(13.5);
        destination.setPlaceAddress("Destination");

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