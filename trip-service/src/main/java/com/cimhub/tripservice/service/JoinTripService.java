package com.cimhub.tripservice.service;

import com.cimhub.common.exception.BusinessException;
import com.cimhub.common.models.Points;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
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
public class JoinTripService {

        private final TripsRepository tripsRepository;
        private final BookingRepository bookingRepository;

        @Transactional
        public String joinTrip(JoinTripRequest request) {
                log.info("Processing join trip request for passenger: {}, tripId: {}",
                                request.passengerId(), request.tripId());

                Trip trip = tripsRepository.findById(request.tripId())
                                .orElseThrow(() -> BusinessException.notFound("Trip", request.tripId()));

                if (!"OFFERED".equals(trip.getTripStatus()) && !"AVAILABLE".equals(trip.getTripStatus())) {
                        throw new BusinessException(
                                        "TRIP_NOT_AVAILABLE",
                                        "Trip is not available for booking",
                                        HttpStatus.BAD_REQUEST);
                }

                List<Booking> existingBookings = bookingRepository.findByTripId(request.tripId());
                boolean alreadyBooked = existingBookings.stream()
                                .anyMatch(b -> b.getPassengerId().equals(request.passengerId()) &&
                                                b.getStatus() != BookingStatus.CANCELLED);

                if (alreadyBooked) {
                        throw new BusinessException(
                                        "ALREADY_BOOKED",
                                        "You have already booked this trip",
                                        HttpStatus.CONFLICT);
                }

                int requestedSeats = request.requestedSeats();
                int availableSeats = trip.getOfferedSeat() - trip.getCurrSeats();

                if (availableSeats < requestedSeats) {
                        throw new BusinessException(
                                        "INSUFFICIENT_SEATS",
                                        String.format("Only %d seats available, but %d requested", availableSeats,
                                                        requestedSeats),
                                        HttpStatus.BAD_REQUEST);
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
                                .pickupLocation(pickupPoints)
                                .dropoffLocation(dropoffPoints)
                                .requestedSeats(requestedSeats)
                                .status(BookingStatus.CONFIRMED)
                                .createdAt(Instant.now())
                                .build();

                bookingRepository.save(booking);

                trip.setCurrSeats(trip.getCurrSeats() + requestedSeats);
                tripsRepository.save(trip);

                log.info("Passenger {} successfully joined trip {}. Seats booked: {}",
                                request.passengerId(), request.tripId(), requestedSeats);

                return "Successfully joined the trip. Booking confirmed!";
        }
}
