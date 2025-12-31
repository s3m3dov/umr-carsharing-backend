package com.cimhub.tripservice.service;

import com.cimhub.common.exception.BusinessException;
import com.cimhub.tripservice.dto.request.CancelTripRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

        private final BookingRepository bookingRepository;
        private final TripsRepository tripsRepository;

        @Transactional
        public String cancelRide(CancelTripRequest request) {
                String passengerId = request.userId();
                String bookingId = request.rideId();

                log.info("Processing booking cancellation for passenger: {}, bookingId: {}",
                                passengerId, bookingId);

                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> BusinessException.notFound("Booking", bookingId));

                if (!booking.getPassengerId().equals(passengerId)) {
                        throw new BusinessException(
                                        "UNAUTHORIZED",
                                        "You are not authorized to cancel this booking",
                                        HttpStatus.FORBIDDEN);
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                        throw new BusinessException(
                                        "ALREADY_CANCELLED",
                                        "This booking has already been cancelled",
                                        HttpStatus.BAD_REQUEST);
                }

                booking.setStatus(BookingStatus.CANCELLED);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);

                Trip trip = tripsRepository.findById(booking.getTripId())
                                .orElseThrow(() -> BusinessException.notFound("Trip", booking.getTripId()));

                trip.setCurrSeats(trip.getCurrSeats() - booking.getRequestedSeats());
                tripsRepository.save(trip);

                log.info("Booking {} cancelled successfully by passenger {}", bookingId, passengerId);
                return "Booking cancelled successfully";
        }

        @Transactional
        public String cancelTrip(CancelTripRequest request) {
                String driverId = request.userId();
                String tripId = request.tripId();

                log.info("Processing trip cancellation for driver: {}, tripId: {}", driverId, tripId);

                Trip trip = tripsRepository.findById(tripId)
                                .orElseThrow(() -> BusinessException.notFound("Trip", tripId));

                if (!trip.getDriverId().equals(driverId)) {
                        throw new BusinessException(
                                        "UNAUTHORIZED",
                                        "You are not authorized to cancel this trip",
                                        HttpStatus.FORBIDDEN);
                }

                if ("CANCELLED".equals(trip.getTripStatus())) {
                        throw new BusinessException(
                                        "ALREADY_CANCELLED",
                                        "This trip has already been cancelled",
                                        HttpStatus.BAD_REQUEST);
                }

                List<Booking> bookings = bookingRepository.findByTripId(tripId);
                for (Booking booking : bookings) {
                        if (booking.getStatus() != BookingStatus.CANCELLED) {
                                booking.setStatus(BookingStatus.CANCELLED);
                                booking.setUpdatedAt(Instant.now());
                                bookingRepository.save(booking);
                        }
                }

                trip.setTripStatus("CANCELLED");
                tripsRepository.save(trip);

                log.info("Trip {} cancelled successfully by driver {}. {} bookings cancelled",
                                tripId, driverId, bookings.size());

                return String.format("Trip cancelled successfully. %d passenger bookings have been cancelled",
                                bookings.size());
        }
}
