package com.cimhub.tripservice.dto.response;

import com.cimhub.common.models.Points;
import com.cimhub.tripservice.model.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PassengerRideResponseTest {

    @Test
    void builder_shouldCreateResponseWithDriverDetails() {
        Points pickup = new Points();
        pickup.setLatitude(52.5);
        pickup.setLongitude(13.4);

        Points dropoff = new Points();
        dropoff.setLatitude(52.6);
        dropoff.setLongitude(13.5);

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
