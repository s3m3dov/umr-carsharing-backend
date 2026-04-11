package com.cimhub.tripservice.service.trip;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.mapper.BookingMapper;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveRidesService {

    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;
    private final TripMapper tripMapper;
    private final BookingMapper bookingMapper;

    public List<DriverTripResponse> findDriverActiveTrips(String driverId) {
        log.info("Fetching active trips for driver: {}", driverId);
        List<Trip> activeTrips = tripsRepository.findByDriverIdAndTripStatusIn(
                driverId, List.of(TripStatus.CREATED, TripStatus.AVAILABLE, TripStatus.IN_PROGRESS));
        return activeTrips.stream().map(tripMapper::toDriverTripResponse).collect(Collectors.toList());
    }

    public List<DriverTripResponse> findDriverTripHistory(String driverId) {
        log.info("Fetching trip history for driver: {}", driverId);
        List<Trip> pastTrips = tripsRepository.findByDriverIdAndTripStatusIn(
                driverId, List.of(TripStatus.COMPLETED, TripStatus.CANCELLED));
        return pastTrips.stream().map(tripMapper::toDriverTripResponse).collect(Collectors.toList());
    }

    public List<PassengerRideResponse> findPassengerActiveRides(String passengerId) {
        log.info("Fetching active rides for passenger: {}", passengerId);
        List<Booking> activeBookings = bookingRepository.findByPassengerIdAndStatusIn(
                passengerId, List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS));
        return buildPassengerRideResponses(activeBookings);
    }

    public List<PassengerRideResponse> findPassengerRideHistory(String passengerId) {
        log.info("Fetching ride history for passenger: {}", passengerId);
        List<Booking> pastBookings = bookingRepository.findByPassengerIdAndStatusIn(
                passengerId, List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
        return buildPassengerRideResponses(pastBookings);
    }

    private List<PassengerRideResponse> buildPassengerRideResponses(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return List.of();
        }

        List<String> tripIds =
                bookings.stream().map(Booking::getTripId).distinct().toList();
        Map<String, Trip> tripMap = tripsRepository.findAllById(tripIds).stream()
                .collect(Collectors.toMap(Trip::getTripId, Function.identity()));

        return bookings.stream()
                .map(booking -> bookingMapper.toPassengerRideResponse(booking, tripMap.get(booking.getTripId())))
                .collect(Collectors.toList());
    }
}
