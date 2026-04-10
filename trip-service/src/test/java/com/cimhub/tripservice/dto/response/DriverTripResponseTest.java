package com.cimhub.tripservice.dto.response;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.model.TripStatus;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DriverTripResponseTest {

    @Test
    void builder_shouldCreateResponseWithPassengerInfo() {
        Points source = new Points();
        source.setLatitude(52.5);
        source.setLongitude(13.4);

        Points destination = new Points();
        destination.setLatitude(52.6);
        destination.setLongitude(13.5);

        DriverTripResponse.PassengerInfo passengerInfo = DriverTripResponse.PassengerInfo.builder()
                .userId("p-1")
                .bookedSeats(2)
                .pickupLocation(source)
                .dropoffLocation(destination)
                .build();

        DriverTripResponse response = DriverTripResponse.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .vehicleNumber("HE-XX-1234")
                .carType("SEDAN")
                .tripStatus(TripStatus.AVAILABLE)
                .sourceAddress(source)
                .destinationAddress(destination)
                .tripStartDateTime(ZonedDateTime.parse("2026-09-01T10:00:00Z"))
                .tripTimezone("UTC")
                .totalSeats(4)
                .availableSeats(2)
                .bookedSeats(2)
                .passengers(List.of(passengerInfo))
                .routeDistanceInKm(12.3)
                .routeDurationInMinutes(25.0)
                .pricePerSeat(10.0)
                .estimatedEarnings(20.0)
                .build();

        assertEquals("trip-1", response.tripId());
        assertEquals("driver-1", response.driverId());
        assertEquals(TripStatus.AVAILABLE, response.tripStatus());
        assertEquals(1, response.passengers().size());
        assertEquals("p-1", response.passengers().getFirst().userId());
        assertEquals(2, response.passengers().getFirst().bookedSeats());
    }
}
