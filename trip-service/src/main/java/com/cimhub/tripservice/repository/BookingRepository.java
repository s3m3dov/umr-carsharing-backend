package com.cimhub.tripservice.repository;

import com.cimhub.common.enums.BookingStatus;
import com.cimhub.tripservice.model.Booking;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    // Find all bookings for a passenger
    List<Booking> findByPassengerId(String passengerId);

    // Find all bookings for a trip
    List<Booking> findByTripId(String tripId);

    List<Booking> findByTripIdInAndStatusIn(List<String> tripIds, List<BookingStatus> statuses);

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

    // Find bookings by passenger and status
    List<Booking> findByPassengerIdAndStatus(String passengerId, BookingStatus status);

    // Admin paginated queries
    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByPassengerId(String passengerId, Pageable pageable);

    void deleteByPassengerId(String passengerId);

    void deleteByDriverId(String driverId);
}
