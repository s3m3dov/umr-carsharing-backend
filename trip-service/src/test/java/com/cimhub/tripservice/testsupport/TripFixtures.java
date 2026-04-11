package com.cimhub.tripservice.testsupport;

import com.cimhub.common.model.Points;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.common.enums.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.common.enums.TripStatus;

import java.time.Instant;
import java.time.ZonedDateTime;

public final class TripFixtures {

    private TripFixtures() {
    }

    public static Points point(double latitude, double longitude, String placeAddress) {
        return com.cimhub.common.testsupport.Points.of(latitude, longitude, placeAddress);
    }

    public static OfferRideRequest offerRideRequest() {
        return OfferRideRequest.builder()
                .driverId("driver-1")
                .sourceAddress(point(52.5, 13.4, "Berlin"))
                .destinationAddress(point(52.6, 13.5, "Berlin outskirts"))
                .tripStartDateTime(ZonedDateTime.parse("2026-05-01T10:15:30.987Z"))
                .totalSeats(3)
                .pricePerSeat(5.0)
                .build();
    }

    public static JoinTripRequest joinTripRequest(String tripId, String passengerId, int requestedSeats) {
        return JoinTripRequest.builder()
                .tripId(tripId)
                .passengerId(passengerId)
                .requestedSeats(requestedSeats)
                .pickupPoint(point(50.8154, 8.6747, "Marburg"))
                .destinationPoint(point(50.7265, 7.0895, "Frankfurt"))
                .build();
    }

    public static Trip trip(String tripId, String driverId, TripStatus status, int totalSeats, int bookedSeats) {
        Trip trip = new Trip();
        trip.setTripId(tripId);
        trip.setDriverId(driverId);
        trip.setTripStatus(status);
        trip.setTotalSeats(totalSeats);
        trip.setBookedSeats(bookedSeats);
        trip.setPricePerSeat(10.0);
        trip.setSourceAddress(point(52.5, 13.4, "Source"));
        trip.setDestinationAddress(point(52.6, 13.5, "Destination"));
        return trip;
    }

    public static Booking booking() {
        Booking booking = new Booking();
        booking.setBookingId("b-1");
        booking.setTripId("trip-1");
        booking.setDriverId("driver-1");
        booking.setPassengerId("passenger-1");
        booking.setRequestedSeats(2);
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setPickupLocation(point(50.8154, 8.6747, "Marburg"));
        booking.setDropoffLocation(point(50.7265, 7.0895, "Frankfurt"));
        booking.setCreatedAt(Instant.parse("2030-01-01T00:00:00Z"));
        booking.setUpdatedAt(Instant.parse("2030-01-01T00:00:00Z"));
        return booking;
    }
}
