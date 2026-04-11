package com.cimhub.tripservice.dto.response;

import static com.cimhub.common.testsupport.Points.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.model.Points;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class PassengerRideResponseTest {

    @Test
    void builder_shouldCreateResponseWithDriverDetails() {
        Points pickup = of(52.5, 13.4, "Pickup");
        Points dropoff = of(52.6, 13.5, "Destination");

        PassengerRideResponse.DriverDetails driverDetails = PassengerRideResponse.DriverDetails.builder()
                .name("John D.")
                .rating(4.8)
                .totalTrips(42)
                .build();

        PassengerRideResponse response = PassengerRideResponse.builder()
                .rideId("ride-1")
                .tripId("trip-1")
                .driverId("driver-1")
                .vehicleNumber("HE-XX-1234")
                .carType("SEDAN")
                .rideStatus(BookingStatus.CONFIRMED)
                .pickupLocation(pickup)
                .dropoffLocation(dropoff)
                .tripStartDateTime(ZonedDateTime.parse("2026-10-01T10:00:00Z"))
                .tripTimezone("UTC")
                .bookedSeats(1)
                .rideDistanceInKm(10.0)
                .rideDurationInMinutes(18.0)
                .pricePerSeat(12.5)
                .estimatedFare(12.5)
                .driverDetails(driverDetails)
                .build();

        assertEquals("ride-1", response.rideId());
        assertEquals("trip-1", response.tripId());
        assertEquals(BookingStatus.CONFIRMED, response.rideStatus());
        assertEquals("John D.", response.driverDetails().name());
        assertEquals(4.8, response.driverDetails().rating());
        assertEquals(42, response.driverDetails().totalTrips());
    }
}
