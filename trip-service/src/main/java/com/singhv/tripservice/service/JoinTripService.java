package com.singhv.tripservice.service;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.common.models.Points;
import com.singhv.tripservice.dto.JoinTripRequestDTO;
import com.singhv.tripservice.model.Rides;
import com.singhv.tripservice.model.Trips;
import com.singhv.tripservice.repository.RidesRepository;
import com.singhv.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinTripService {

    private final TripsRepository tripsRepository;
    private final RidesRepository ridesRepository;

    @Transactional
    public ResponseDTO<String> joinTrip(RequestDTO<JoinTripRequestDTO> request) {
        ResponseDTO<String> response = new ResponseDTO<>();
        
        try {
            String passengerId = request.getUserId();
            JoinTripRequestDTO requestContent = request.getRequestContent();
            
            log.info("Processing join trip request for passenger: {}, tripId: {}", 
                    passengerId, requestContent.getTripId());
            
            // Step 1: Find the trip
            Optional<Trips> tripOpt = tripsRepository.findById(requestContent.getTripId());
            if (tripOpt.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("Trip not found");
                return response;
            }
            
            Trips trip = tripOpt.get();
            
            // Step 2: Basic validations
            // Don't let driver join their own trip
            if (trip.getDriverId().equals(passengerId)) {
                response.setSuccess(false);
                response.setErrorMessage("You cannot join your own trip");
                return response;
            }
            
            // Check if already booked
            if (ridesRepository.existsByPassengerIdAndTripId(passengerId, requestContent.getTripId())) {
                response.setSuccess(false);
                response.setErrorMessage("You have already booked this trip");
                return response;
            }
            
            // Check seat availability
            int availableSeats = trip.getOfferedSeat() - trip.getCurrSeats();
            if (availableSeats < requestContent.getRequestedSeats()) {
                response.setSuccess(false);
                response.setErrorMessage("Not enough seats available. Available: " + availableSeats);
                return response;
            }
            
            // Step 3: Parse ride start time
            Instant rideStartTimeUTC;
            String timezone;
            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(requestContent.getRideStartTime());
                rideStartTimeUTC = zonedDateTime.toInstant();
                timezone = zonedDateTime.getZone().getId();
            } catch (Exception e) {
                log.error("Error parsing ride start time: {}", requestContent.getRideStartTime(), e);
                response.setSuccess(false);
                response.setErrorMessage("Invalid ride start time format");
                return response;
            }
            
            // Step 4: Create pickup and dropoff Points
            Points pickupLocation = new Points();
            pickupLocation.setLatitude(requestContent.getPickupPoint().getLatitude());
            pickupLocation.setLongitude(requestContent.getPickupPoint().getLongitude());
            pickupLocation.setPlaceAddress(requestContent.getPickupPoint().getPlaceAddress());
            
            Points dropoffLocation = new Points();
            dropoffLocation.setLatitude(requestContent.getDestinationPoint().getLatitude());
            dropoffLocation.setLongitude(requestContent.getDestinationPoint().getLongitude());
            dropoffLocation.setPlaceAddress(requestContent.getDestinationPoint().getPlaceAddress());
            
            // Step 5: Create the ride booking
            Rides ride = Rides.builder()
                    .passengerId(passengerId)
                    .tripId(requestContent.getTripId())
                    .driverId(requestContent.getDriverId())
                    .pickupLocation(pickupLocation)
                    .dropoffLocation(dropoffLocation)
                    .requestedSeats(requestContent.getRequestedSeats())
                    .rideStatus("CONFIRMED")
                    .rideStartTimeUTC(rideStartTimeUTC)
                    .rideTimezone(timezone)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();
            
            // Save the ride
            Rides savedRide = ridesRepository.save(ride);
            log.info("Ride booking created: {}", savedRide.getRideId());
            
            // Step 5: Update the trip - increment currSeats and add to joinedRidersId
            trip.setCurrSeats(trip.getCurrSeats() + requestContent.getRequestedSeats());
            
            // Add passenger to joinedRidersId list
            if (trip.getJoinedRidersId() == null) {
                trip.setJoinedRidersId(new ArrayList<>());
            }
            trip.getJoinedRidersId().add(new ObjectId(savedRide.getRideId()));
            
            tripsRepository.save(trip);
            log.info("Trip updated: tripId={}, newCurrSeats={}", trip.getTripId(), trip.getCurrSeats());
            
            // Success response
            response.setSuccess(true);
            response.setResponseContent("Ride booked successfully! Ride ID: " + savedRide.getRideId());
            
        } catch (Exception e) {
            log.error("Error processing join trip request", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to join trip: " + e.getMessage());
        }
        
        return response;
    }
}
