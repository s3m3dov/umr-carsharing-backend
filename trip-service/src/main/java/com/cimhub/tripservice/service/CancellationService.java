package com.cimhub.tripservice.service;

import com.cimhub.common.dto.RequestDTO;
import com.cimhub.common.dto.ResponseDTO;
import com.cimhub.tripservice.dto.CancelRequestDTO;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final TripsRepository tripsRepository;

    /**
     * Passenger cancels their ride
     */
    @Transactional
    public ResponseDTO<String> cancelRide(RequestDTO<CancelRequestDTO> request) {
        ResponseDTO<String> response = new ResponseDTO<>();

        try {
            String passengerId = request.getUserId();
            CancelRequestDTO cancelRequest = request.getRequestContent();

            log.info("Processing ride cancellation for passenger: {}, tripId: {}",
                    passengerId, cancelRequest.getTripId());

            // Find the booking
            List<Booking> bookings = bookingRepository.findByPassengerIdAndTripId(passengerId,
                    cancelRequest.getTripId());

            if (bookings.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("No booking found for this trip");
                return response;
            }

            Booking booking = bookings.get(0);

            // Check if already cancelled
            if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
                response.setSuccess(false);
                response.setErrorMessage("Booking is already cancelled");
                return response;
            }

            // Update booking status to CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);

            log.info("Booking cancelled: {}", booking.getBookingId());

            // Update trip - decrease currSeats
            Optional<Trip> tripOpt = tripsRepository.findById(cancelRequest.getTripId());
            if (tripOpt.isPresent()) {
                Trip trip = tripOpt.get();
                int newCurrSeats = trip.getCurrSeats() - booking.getRequestedSeats();
                trip.setCurrSeats(Math.max(0, newCurrSeats)); // Ensure it doesn't go negative
                tripsRepository.save(trip);

                log.info("Trip updated: tripId={}, newCurrSeats={}", trip.getTripId(), trip.getCurrSeats());
            }

            response.setSuccess(true);
            response.setResponseContent("Booking cancelled successfully. Booking ID: " + booking.getBookingId());

        } catch (Exception e) {
            log.error("Error cancelling ride", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to cancel ride: " + e.getMessage());
        }

        return response;
    }

    /**
     * Driver cancels their trip (cancels all associated rides)
     */
    @Transactional
    public ResponseDTO<String> cancelTrip(RequestDTO<CancelRequestDTO> request) {
        ResponseDTO<String> response = new ResponseDTO<>();

        try {
            String driverId = request.getUserId();
            CancelRequestDTO cancelRequest = request.getRequestContent();

            log.info("Processing trip cancellation for driver: {}, tripId: {}",
                    driverId, cancelRequest.getTripId());

            // Find the trip
            Optional<Trip> tripOpt = tripsRepository.findById(cancelRequest.getTripId());

            if (tripOpt.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("Trip not found");
                return response;
            }

            Trip trip = tripOpt.get();

            // Verify the driver owns this trip
            if (!trip.getDriverId().equals(driverId)) {
                response.setSuccess(false);
                response.setErrorMessage("You are not authorized to cancel this trip");
                return response;
            }

            // Check if already cancelled
            if ("CANCELLED".equals(trip.getTripStatus())) {
                response.setSuccess(false);
                response.setErrorMessage("Trip is already cancelled");
                return response;
            }

            // Update trip status to CANCELLED
            trip.setTripStatus("CANCELLED");
            tripsRepository.save(trip);

            log.info("Trip cancelled: {}", trip.getTripId());

            // Cancel all bookings for this trip
            List<Booking> bookings = bookingRepository.findByTripId(cancelRequest.getTripId());
            int cancelledBookingsCount = 0;

            for (Booking booking : bookings) {
                if (!BookingStatus.CANCELLED.equals(booking.getStatus())) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.setUpdatedAt(Instant.now());
                    bookingRepository.save(booking);
                    cancelledBookingsCount++;
                }
            }

            log.info("Cancelled {} bookings for trip: {}", cancelledBookingsCount, trip.getTripId());

            response.setSuccess(true);
            response.setResponseContent(String.format(
                    "Trip cancelled successfully. Trip ID: %s. %d passenger booking(s) cancelled.",
                    trip.getTripId(), cancelledBookingsCount));

        } catch (Exception e) {
            log.error("Error cancelling trip", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to cancel trip: " + e.getMessage());
        }

        return response;
    }
}
