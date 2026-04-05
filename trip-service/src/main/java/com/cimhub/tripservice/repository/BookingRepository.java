package com.cimhub.tripservice.repository;

import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    // Find all bookings for a passenger
    List<Booking> findByPassengerId(String passengerId);

    // Find all bookings for a trip
    List<Booking> findByTripId(String tripId);

    // Check if passenger already booked this trip
    boolean existsByPassengerIdAndTripId(String passengerId, String tripId);

    // Find bookings by passenger and trip
    List<Booking> findByPassengerIdAndTripId(String passengerId, String tripId);

    // Find bookings by passenger and time range
    List<Booking> findByPassengerIdAndRideStartTimeUTCBetween(
            @Param("passengerId") @NotBlank String passengerId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    List<Booking> findByPassengerIdAndStatusIn(String passengerId, List<BookingStatus> statuses);

    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    // Find bookings by passenger and status
    List<Booking> findByPassengerIdAndStatus(String passengerId, BookingStatus status);

    // Admin paginated queries
    org.springframework.data.domain.Page<Booking> findAll(org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Booking> findByPassengerId(String passengerId, org.springframework.data.domain.Pageable pageable);

    long countByStatus(BookingStatus status);
}
