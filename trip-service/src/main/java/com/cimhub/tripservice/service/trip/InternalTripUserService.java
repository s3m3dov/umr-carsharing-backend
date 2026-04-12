package com.cimhub.tripservice.service.trip;

import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.PassengerRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.repository.VehicleDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalTripUserService {

    private final TripsRepository tripsRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;

    @Transactional
    public void deleteUserData(String userId) {
        log.info("Deleting all trip-related data for user: {}", userId);

        // Delete user's trips as a driver
        tripsRepository.deleteByDriverId(userId);

        // Delete user's bookings (as a passenger OR as a driver of a trip being booked)
        bookingRepository.deleteByPassengerId(userId);
        bookingRepository.deleteByDriverId(userId);

        // Delete user's passenger profile replication
        passengerRepository.deleteByUserId(userId);

        // Delete user's vehicle/document replication
        vehicleDocumentRepository.deleteByUserId(userId);

        log.info("Successfully deleted all trip-related data for user: {}", userId);
    }
}
