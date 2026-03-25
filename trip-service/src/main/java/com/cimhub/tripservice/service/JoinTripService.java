package com.cimhub.tripservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.exception.AlreadyBookedException;
import com.cimhub.tripservice.exception.InsufficientSeatsException;
import com.cimhub.tripservice.exception.TripNotAvailableException;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinTripService {

    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public String joinTrip(JoinTripRequest request) {
        log.info("Processing join trip request for passenger: {}, tripId: {}, requestedSeats: {}",
                request.passengerId(), request.tripId(), request.requestedSeats());

        Trip trip = tripsRepository.findById(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip not found with id: " + request.tripId()));

        if (trip.getTripStatus() != TripStatus.OFFERED && trip.getTripStatus() != TripStatus.AVAILABLE) {
            throw new TripNotAvailableException("Trip is not available for booking");
        }

        List<Booking> existingBookings = bookingRepository.findByTripId(request.tripId());
        boolean alreadyBooked = existingBookings.stream()
                .anyMatch(b -> b.getPassengerId().equals(request.passengerId()) &&
                        b.getStatus() != BookingStatus.CANCELLED);

        if (alreadyBooked) {
            throw new AlreadyBookedException("You have already booked this trip");
        }

        int requestedSeats = request.requestedSeats();
        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();

        if (availableSeats < requestedSeats) {
            throw new InsufficientSeatsException(
                    String.format("Only %d seats available, but %d requested", availableSeats,
                            requestedSeats));
        }

        Points pickupPoints = new Points();
        pickupPoints.setLatitude(request.pickupPoint().latitude());
        pickupPoints.setLongitude(request.pickupPoint().longitude());
        pickupPoints.setPlaceAddress(request.pickupPoint().placeAddress());

        Points dropoffPoints = new Points();
        dropoffPoints.setLatitude(request.destinationPoint().latitude());
        dropoffPoints.setLongitude(request.destinationPoint().longitude());
        dropoffPoints.setPlaceAddress(request.destinationPoint().placeAddress());

        Booking booking = Booking.builder()
                .tripId(request.tripId())
                .passengerId(request.passengerId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .pickupLocation(pickupPoints)
                .dropoffLocation(dropoffPoints)
                .requestedSeats(requestedSeats)
                .status(BookingStatus.REQUESTED)
                .rideStartTimeUTC(trip.getTripStartDateTimeUTC())
                .rideTimezone(trip.getTripTimezone())
                .createdAt(Instant.now())
                .build();

        bookingRepository.save(booking);

        trip.setBookedSeats(trip.getBookedSeats() + requestedSeats);
        tripsRepository.save(trip);

        log.info("Passenger {} requested to join trip {}. Seats requested: {}",
                request.passengerId(), request.tripId(), requestedSeats);

        return "Booking request submitted. Waiting for driver confirmation.";
    }

    @Transactional
    public String acceptBooking(String bookingId) {
        log.info("Accepting booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new IllegalStateException("Booking is not in REQUESTED state");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        log.info("Booking {} accepted", bookingId);
        return "Booking accepted successfully";
    }

    @Transactional
    public String rejectBooking(String bookingId) {
        log.info("Rejecting booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new IllegalStateException("Booking is not in REQUESTED state");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        Trip trip = tripsRepository.findById(booking.getTripId()).orElse(null);
        if (trip != null) {
            trip.setBookedSeats(trip.getBookedSeats() - booking.getRequestedSeats());
            tripsRepository.save(trip);
        }

        log.info("Booking {} rejected", bookingId);
        return "Booking rejected";
    }
}
