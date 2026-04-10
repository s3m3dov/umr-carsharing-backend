package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.model.Points;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.exception.AlreadyBookedException;
import com.cimhub.tripservice.exception.BookingAlreadyProcessedException;
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
    private final AuditService auditService;
    private final DriverNotificationService driverNotificationService;

    /**
     * Books a ride with automatic confirmation — the passenger's seat is reserved
     * immediately without requiring driver approval.
     */
    @Transactional
    public String joinTrip(JoinTripRequest request) {
        log.info("Processing auto-confirm join trip request for passenger: {}, tripId: {}, requestedSeats: {}",
                request.passengerId(), request.tripId(), request.requestedSeats());

        Trip trip = validateAndFetchTrip(request);
        Booking booking = buildBooking(request, trip, BookingStatus.CONFIRMED);

        bookingRepository.save(booking);
        auditService.logBook(EntityType.BOOKING, booking.getBookingId(), request.passengerId(),
                "Booking auto-confirmed for passenger");

        driverNotificationService.notifyBookingConfirmed(trip, booking);

        trip.setBookedSeats(trip.getBookedSeats() + request.requestedSeats());
        trip.setUpdatedAt(Instant.now());
        tripsRepository.save(trip);

        log.info("Passenger {} joined trip {} (auto-confirmed). Seats: {}",
                request.passengerId(), request.tripId(), request.requestedSeats());

        return "Ride booked successfully.";
    }

    /**
     * Books a ride using the driver-accept mechanism — the booking is created in
     * REQUESTED state and the driver must explicitly accept or reject it.
     */
    @Transactional
    public String joinTripWithDriverAccept(JoinTripRequest request) {
        log.info("Processing driver-accept join trip request for passenger: {}, tripId: {}, requestedSeats: {}",
                request.passengerId(), request.tripId(), request.requestedSeats());

        Trip trip = validateAndFetchTrip(request);
        Booking booking = buildBooking(request, trip, BookingStatus.REQUESTED);

        bookingRepository.save(booking);
        auditService.logBook(EntityType.BOOKING, booking.getBookingId(), request.passengerId(),
                "Booking request submitted by passenger");

        driverNotificationService.notifyBookingRequested(trip, booking);

        trip.setBookedSeats(trip.getBookedSeats() + request.requestedSeats());
        trip.setUpdatedAt(Instant.now());
        tripsRepository.save(trip);

        log.info("Passenger {} requested to join trip {} (awaiting driver accept). Seats requested: {}",
                request.passengerId(), request.tripId(), request.requestedSeats());

        return "Booking request submitted. Waiting for driver confirmation.";
    }

    @Transactional
    public String acceptBooking(String bookingId, String driverId) {
        log.info("Driver {} accepting booking: {}", driverId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (!driverId.equals(booking.getDriverId())) {
            throw new UnauthorizedException("You are not authorized to accept this booking");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BookingAlreadyProcessedException(
                    "Booking " + bookingId + " is not in REQUESTED state (current: " + booking.getStatus() + ")");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        auditService.logApprove(EntityType.BOOKING, bookingId, driverId, "Booking accepted by driver");

        Trip trip = tripsRepository.findById(booking.getTripId()).orElse(null);
        if (trip != null) {
            driverNotificationService.notifyPassengerBookingAccepted(trip, booking);
        }

        log.info("Booking {} accepted by driver {}", bookingId, driverId);
        return "Booking accepted successfully";
    }

    @Transactional
    public String rejectBooking(String bookingId, String driverId) {
        log.info("Driver {} rejecting booking: {}", driverId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (!driverId.equals(booking.getDriverId())) {
            throw new UnauthorizedException("You are not authorized to reject this booking");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BookingAlreadyProcessedException(
                    "Booking " + bookingId + " is not in REQUESTED state (current: " + booking.getStatus() + ")");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        auditService.logReject(EntityType.BOOKING, bookingId, driverId, "Booking rejected by driver");

        Trip trip = tripsRepository.findById(booking.getTripId()).orElse(null);
        if (trip != null) {
            driverNotificationService.notifyPassengerBookingRejected(trip, booking);
            trip.setBookedSeats(trip.getBookedSeats() - booking.getRequestedSeats());
            trip.setUpdatedAt(Instant.now());
            tripsRepository.save(trip);
        }

        log.info("Booking {} rejected by driver {}", bookingId, driverId);
        return "Booking rejected";
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Trip validateAndFetchTrip(JoinTripRequest request) {
        Trip trip = tripsRepository.findById(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trip not found with id: " + request.tripId()));

        if (trip.getTripStatus() != TripStatus.CREATED && trip.getTripStatus() != TripStatus.AVAILABLE) {
            throw new TripNotAvailableException("Trip is not available for booking");
        }

        List<Booking> existingBookings = bookingRepository.findByTripId(request.tripId());
        boolean alreadyBooked = existingBookings.stream()
                .anyMatch(b -> b.getPassengerId().equals(request.passengerId()) &&
                        b.getStatus() != BookingStatus.CANCELLED);

        if (alreadyBooked) {
            throw new AlreadyBookedException("You have already booked this trip");
        }

        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();
        if (availableSeats < request.requestedSeats()) {
            throw new InsufficientSeatsException(
                    String.format("Only %d seats available, but %d requested", availableSeats,
                            request.requestedSeats()));
        }

        return trip;
    }

    private Booking buildBooking(JoinTripRequest request, Trip trip, BookingStatus status) {
        Points pickupPoints = new Points();
        pickupPoints.setLatitude(request.pickupPoint().getLatitude());
        pickupPoints.setLongitude(request.pickupPoint().getLongitude());
        pickupPoints.setPlaceAddress(request.pickupPoint().getPlaceAddress());

        Points dropoffPoints = new Points();
        dropoffPoints.setLatitude(request.destinationPoint().getLatitude());
        dropoffPoints.setLongitude(request.destinationPoint().getLongitude());
        dropoffPoints.setPlaceAddress(request.destinationPoint().getPlaceAddress());

        return Booking.builder()
                .tripId(request.tripId())
                .passengerId(request.passengerId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .pickupLocation(pickupPoints)
                .dropoffLocation(dropoffPoints)
                .requestedSeats(request.requestedSeats())
                .status(status)
                .rideStartTimeUTC(trip.getTripStartDateTimeUTC())
                .rideTimezone(trip.getTripTimezone())
                .pricePerSeat(trip.getPricePerSeat())
                .estimatedPrice((double) request.requestedSeats() * trip.getPricePerSeat())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
