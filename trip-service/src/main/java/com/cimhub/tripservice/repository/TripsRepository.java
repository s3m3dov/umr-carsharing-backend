package com.cimhub.tripservice.repository;

import com.cimhub.tripservice.model.Trip;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
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

    // You can keep the old method for backward compatibility if needed, but it's
    // not timezone-safe
    // List<Trips> findByDriverIdAndTripStartDate(@Param("driverId") @NotBlank
    // String driverId, @Param("tripStartDate") Date tripStartDate);
}