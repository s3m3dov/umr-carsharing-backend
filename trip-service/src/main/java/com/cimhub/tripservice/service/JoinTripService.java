package com.cimhub.tripservice.service;

import com.cimhub.common.exception.BusinessException;
import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.JoinTripRequestDTO;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service for passengers joining trips
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JoinTripService {

    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public String joinTrip(JoinTripRequestDTO requestContent) {
        String passengerId = requestContent.getPassengerId();

        log.info("Processing join trip request for passenger: {}, tripId: {}",
                passengerId, requestContent.getTripId());

        // Step 1: Find the trip
        Trip trip = tripsRepository.findById(requestContent.getTripId())
                .orElseThrow(() -> BusinessException.notFound("Trip", requestContent.getTripId()));

        // Step 2: Validate trip is available
        if (!"OFFERED".equals(trip.getTripStatus())) {
            throw new BusinessException(
                    "TRIP_NOT_AVAILABLE",
                    "Trip is not available for booking",
                    HttpStatus.BAD_REQUEST);
        }

        // Step 3: Check if passenger already has a booking for this trip
        List<Booking> existingBookings = bookingRepository.findByTripId(requestContent.getTripId());
        boolean alreadyBooked = existingBookings.stream()
                .anyMatch(b -> b.getPassengerId().equals(passengerId) &&
                        b.getStatus() != BookingStatus.CANCELLED);

        if (alreadyBooked) {
            throw new BusinessException(
                    "ALREADY_BOOKED",
                    "You have already booked this trip",
                    HttpStatus.CONFLICT);
        }

        // Step 4: Check available seats
        int requestedSeats = requestContent.getRequestedSeats();
        int availableSeats = trip.getOfferedSeat() - trip.getCurrSeats();

        if (availableSeats < requestedSeats) {
            throw new BusinessException(
                    "INSUFFICIENT_SEATS",
                    String.format("Only %d seats available, but %d requested", availableSeats, requestedSeats),
                    HttpStatus.BAD_REQUEST);
        }

        // Step 5: Create booking
        Points pickupPoints = new Points();
        pickupPoints.setLatitude(requestContent.getPickupPoint().getLatitude());
        pickupPoints.setLongitude(requestContent.getPickupPoint().getLongitude());
        pickupPoints.setPlaceAddress(requestContent.getPickupPoint().getPlaceAddress());

        Points dropoffPoints = new Points();
        dropoffPoints.setLatitude(requestContent.getDestinationPoint().getLatitude());
        dropoffPoints.setLongitude(requestContent.getDestinationPoint().getLongitude());
        dropoffPoints.setPlaceAddress(requestContent.getDestinationPoint().getPlaceAddress());

        Booking booking = Booking.builder()
                .tripId(requestContent.getTripId())
                .passengerId(passengerId)
                .driverId(trip.getDriverId())
                .pickupLocation(pickupPoints)
                .dropoffLocation(dropoffPoints)
                .requestedSeats(requestedSeats)
                .status(BookingStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build();

        bookingRepository.save(booking);

        // Step 6: Update trip seat count
        trip.setCurrSeats(trip.getCurrSeats() + requestedSeats);
        tripsRepository.save(trip);

        log.info("Passenger {} successfully joined trip {}. Seats booked: {}",
                passengerId, requestContent.getTripId(), requestedSeats);

        return "Successfully joined the trip. Booking confirmed!";
    }
}
