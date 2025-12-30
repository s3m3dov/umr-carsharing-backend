package com.singhv.tripservice.repository;

import com.singhv.tripservice.model.Trips;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TripsRepository extends MongoRepository<Trips, String> {
    // New method for timezone-aware querying
    List<Trips> findByDriverIdAndTripStartDateTimeUTCBetween(
            @Param("driverId") @NotBlank String driverId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // You can keep the old method for backward compatibility if needed, but it's not timezone-safe
    // List<Trips> findByDriverIdAndTripStartDate(@Param("driverId") @NotBlank String driverId, @Param("tripStartDate") Date tripStartDate);
}