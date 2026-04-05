package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.exception.RideNotCompletableException;
import com.cimhub.tripservice.exception.TripNotInProgressException;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompletionService {

    private static final Set<BookingStatus> COMPLETABLE_RIDE_STATUSES =
            EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS);

    private final BookingRepository bookingRepository;
    private final TripsRepository tripsRepository;
    private final TripService tripService;
    private final AuditService auditService;
    private final DriverNotificationService driverNotificationService;

    /**
     * Driver confirms a specific passenger's drop-off at their stop.
     * The trip continues — only the ride (booking) is completed.
     */
    @Transactional
    public String completeRide(String bookingId, String driverId) {
        log.info("Driver {} completing ride: {}", driverId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (!booking.getDriverId().equals(driverId)) {
            throw new UnauthorizedException("You are not authorized to complete this ride");
        }

        if (!COMPLETABLE_RIDE_STATUSES.contains(booking.getStatus())) {
            throw new RideNotCompletableException(
                    String.format("Ride %s cannot be completed — current status: %s", bookingId, booking.getStatus()));
        }

        Trip trip = tripsRepository.findById(booking.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip not found with id: " + booking.getTripId()));

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        auditService.logComplete(EntityType.BOOKING, bookingId, driverId, "Ride completed by driver");
        driverNotificationService.notifyRideCompleted(trip, booking);

        log.info("Ride {} completed by driver {}", bookingId, driverId);
        return "Ride completed successfully";
    }

    /**
     * Driver ends the full trip at the final destination.
     * All remaining active rides are auto-completed.
     */
    @Transactional
    public String completeTrip(String tripId, String driverId) {
        log.info("Driver {} completing trip: {}", driverId, tripId);

        Trip trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip not found with id: " + tripId));

        if (!trip.getDriverId().equals(driverId)) {
            throw new UnauthorizedException("You are not authorized to complete this trip");
        }

        if (trip.getTripStatus() != TripStatus.IN_PROGRESS && trip.getTripStatus() != TripStatus.AVAILABLE) {
            throw new TripNotInProgressException(
                    String.format("Trip %s cannot be completed — current status: %s", tripId, trip.getTripStatus()));
        }

        if (trip.getTripStatus() == TripStatus.AVAILABLE) {
            tripService.updateStatus(tripId, TripStatus.IN_PROGRESS);
        }

        List<Booking> activeBookings = bookingRepository.findByTripId(tripId).stream()
                .filter(b -> COMPLETABLE_RIDE_STATUSES.contains(b.getStatus()))
                .toList();

        for (Booking booking : activeBookings) {
            booking.setStatus(BookingStatus.COMPLETED);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);
            auditService.logComplete(EntityType.BOOKING, booking.getBookingId(), driverId,
                    "Ride auto-completed on trip completion");
            driverNotificationService.notifyTripCompleted(trip, booking);
        }

        tripService.updateStatus(tripId, TripStatus.COMPLETED);
        auditService.logComplete(EntityType.TRIP, tripId, driverId, "Trip completed by driver");

        log.info("Trip {} completed by driver {}. {} remaining rides auto-completed", tripId, driverId, activeBookings.size());
        return String.format("Trip completed successfully. %d remaining ride(s) were auto-completed", activeBookings.size());
    }
}
