package com.cimhub.tripservice.dto.response;

import static com.cimhub.common.testsupport.Points.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.model.Points;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class DriverTripResponseTest {

    @Test
    void builder_shouldCreateResponseWithPassengerInfo() {
        Points source = of(52.5, 13.4, "Source");
        Points destination = of(52.6, 13.5, "Destination");

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
