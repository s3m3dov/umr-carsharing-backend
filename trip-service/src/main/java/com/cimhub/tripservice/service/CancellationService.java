package com.cimhub.tripservice.service;

import com.cimhub.common.exception.BusinessException;
import com.cimhub.tripservice.dto.CancelRequestDTO;
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
 * Service for handling trip and booking cancellations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final TripsRepository tripsRepository;

    /**
     * Passenger cancels their booking
     */
    @Transactional
    public String cancelRide(CancelRequestDTO request) {
        String passengerId = request.getUserId();
        String bookingId = request.getRideId();

        log.info("Processing booking cancellation for passenger: {}, bookingId: {}",
                passengerId, bookingId);

        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BusinessException.notFound("Booking", bookingId));

        // Verify ownership
        if (!booking.getPassengerId().equals(passengerId)) {
            throw new BusinessException(
                    "UNAUTHORIZED",
                    "You are not authorized to cancel this booking",
                    HttpStatus.FORBIDDEN);
        }

        // Check if already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(
                    "ALREADY_CANCELLED",
                    "This booking has already been cancelled",
                    HttpStatus.BAD_REQUEST);
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        // Update trip seat count
        Trip trip = tripsRepository.findById(booking.getTripId())
                .orElseThrow(() -> BusinessException.notFound("Trip", booking.getTripId()));

        trip.setCurrSeats(trip.getCurrSeats() - booking.getRequestedSeats());
        tripsRepository.save(trip);

        log.info("Booking {} cancelled successfully by passenger {}", bookingId, passengerId);
        return "Booking cancelled successfully";
    }

    /**
     * Driver cancels their trip (cancels all associated bookings)
     */
    @Transactional
    public String cancelTrip(CancelRequestDTO request) {
        String driverId = request.getUserId();
        String tripId = request.getTripId();

        log.info("Processing trip cancellation for driver: {}, tripId: {}", driverId, tripId);

        // Find the trip
        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> BusinessException.notFound("Trip", tripId));

        // Verify ownership
        if (!trip.getDriverId().equals(driverId)) {
            throw new BusinessException(
                    "UNAUTHORIZED",
                    "You are not authorized to cancel this trip",
                    HttpStatus.FORBIDDEN);
        }

        // Check if already cancelled
        if ("CANCELLED".equals(trip.getTripStatus())) {
            throw new BusinessException(
                    "ALREADY_CANCELLED",
                    "This trip has already been cancelled",
                    HttpStatus.BAD_REQUEST);
        }

        // Cancel all bookings for this trip
        List<Booking> bookings = bookingRepository.findByTripId(tripId);
        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);
            }
        }

        // Update trip status
        trip.setTripStatus("CANCELLED");
        tripsRepository.save(trip);

        log.info("Trip {} cancelled successfully by driver {}. {} bookings cancelled",
                tripId, driverId, bookings.size());

        return String.format("Trip cancelled successfully. %d passenger bookings have been cancelled",
                bookings.size());
    }
}
