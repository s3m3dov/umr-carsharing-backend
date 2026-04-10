package com.cimhub.tripservice.repository;

import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TripsRepository extends MongoRepository<Trip, String> {
    // New method for timezone-aware querying
    List<Trip> findByDriverIdAndTripStartDateTimeUTCBetween(
            @Param("driverId") @NotBlank String driverId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    List<Trip> findByDriverIdAndTripStatusIn(String driverId, List<TripStatus> statuses);

    // Admin paginated queries
    Page<Trip> findByTripStatusInAndTripStartDateTimeUTCAfter(
            List<TripStatus> statuses, Instant after, Pageable pageable);

    Page<Trip> findByTripStatusIn(List<TripStatus> statuses, Pageable pageable);

    Page<Trip> findByDriverId(String driverId, Pageable pageable);

    long countByTripStatus(TripStatus status);

    long countByDriverId(String driverId);

    List<Trip> findByDriverIdAndTripStatus(String driverId, TripStatus status);

    @Query(value = "{ 'driverId': { $exists: true, $ne: null } }", fields = "{ 'driverId': 1 }")
    List<Trip> findDistinctDriverIds();
}