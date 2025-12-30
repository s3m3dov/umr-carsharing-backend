package com.singhv.tripservice.repository;

import com.singhv.tripservice.model.Rides;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RidesRepository extends MongoRepository<Rides, String> {
    
    // Find all rides for a passenger
    List<Rides> findByPassengerId(String passengerId);
    
    // Find all rides for a trip
    List<Rides> findByTripId(String tripId);
    
    // Check if passenger already booked this trip
    boolean existsByPassengerIdAndTripId(String passengerId, String tripId);
    
    // Find rides by passenger and trip
    List<Rides> findByPassengerIdAndTripId(String passengerId, String tripId);

    List<Rides> findByPassengerIdAndRideStartTimeUTCBetween(
            @Param("passengerId") @NotBlank String passengerId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
