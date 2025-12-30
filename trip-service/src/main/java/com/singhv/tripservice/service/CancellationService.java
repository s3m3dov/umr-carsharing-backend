package com.singhv.tripservice.service;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.CancelRequestDTO;
import com.singhv.tripservice.model.Rides;
import com.singhv.tripservice.model.Trips;
import com.singhv.tripservice.repository.RidesRepository;
import com.singhv.tripservice.repository.TripsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

    private final RidesRepository ridesRepository;
    private final TripsRepository tripsRepository;

    /**
     * Passenger cancels their ride
     */
    @Transactional
    public ResponseDTO<String> cancelRide(RequestDTO<CancelRequestDTO> request) {
        ResponseDTO<String> response = new ResponseDTO<>();
        
        try {
            String passengerId = request.getUserId();
            CancelRequestDTO cancelRequest = request.getRequestContent();
            
            log.info("Processing ride cancellation for passenger: {}, tripId: {}", 
                    passengerId, cancelRequest.getTripId());
            
            // Find the ride
            List<Rides> rides = ridesRepository.findByPassengerIdAndTripId(passengerId, cancelRequest.getTripId());
            
            if (rides.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("No ride found for this trip");
                return response;
            }
            
            Rides ride = rides.get(0);
            
            // Check if already cancelled
            if ("CANCELLED".equals(ride.getRideStatus())) {
                response.setSuccess(false);
                response.setErrorMessage("Ride is already cancelled");
                return response;
            }
            
            // Update ride status to CANCELLED
            ride.setRideStatus("CANCELLED");
            ride.setUpdatedAt(new Date());
            ridesRepository.save(ride);
            
            log.info("Ride cancelled: {}", ride.getRideId());
            
            // Update trip - decrease currSeats
            Optional<Trips> tripOpt = tripsRepository.findById(cancelRequest.getTripId());
            if (tripOpt.isPresent()) {
                Trips trip = tripOpt.get();
                int newCurrSeats = trip.getCurrSeats() - ride.getRequestedSeats();
                trip.setCurrSeats(Math.max(0, newCurrSeats)); // Ensure it doesn't go negative
                tripsRepository.save(trip);
                
                log.info("Trip updated: tripId={}, newCurrSeats={}", trip.getTripId(), trip.getCurrSeats());
            }
            
            response.setSuccess(true);
            response.setResponseContent("Ride cancelled successfully. Ride ID: " + ride.getRideId());
            
        } catch (Exception e) {
            log.error("Error cancelling ride", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to cancel ride: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Driver cancels their trip (cancels all associated rides)
     */
    @Transactional
    public ResponseDTO<String> cancelTrip(RequestDTO<CancelRequestDTO> request) {
        ResponseDTO<String> response = new ResponseDTO<>();
        
        try {
            String driverId = request.getUserId();
            CancelRequestDTO cancelRequest = request.getRequestContent();
            
            log.info("Processing trip cancellation for driver: {}, tripId: {}", 
                    driverId, cancelRequest.getTripId());
            
            // Find the trip
            Optional<Trips> tripOpt = tripsRepository.findById(cancelRequest.getTripId());
            
            if (tripOpt.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("Trip not found");
                return response;
            }
            
            Trips trip = tripOpt.get();
            
            // Verify the driver owns this trip
            if (!trip.getDriverId().equals(driverId)) {
                response.setSuccess(false);
                response.setErrorMessage("You are not authorized to cancel this trip");
                return response;
            }
            
            // Check if already cancelled
            if ("CANCELLED".equals(trip.getTripStatus())) {
                response.setSuccess(false);
                response.setErrorMessage("Trip is already cancelled");
                return response;
            }
            
            // Update trip status to CANCELLED
            trip.setTripStatus("CANCELLED");
            tripsRepository.save(trip);
            
            log.info("Trip cancelled: {}", trip.getTripId());
            
            // Cancel all rides for this trip
            List<Rides> rides = ridesRepository.findByTripId(cancelRequest.getTripId());
            int cancelledRidesCount = 0;
            
            for (Rides ride : rides) {
                if (!"CANCELLED".equals(ride.getRideStatus())) {
                    ride.setRideStatus("CANCELLED");
                    ride.setUpdatedAt(new Date());
                    ridesRepository.save(ride);
                    cancelledRidesCount++;
                }
            }
            
            log.info("Cancelled {} rides for trip: {}", cancelledRidesCount, trip.getTripId());
            
            response.setSuccess(true);
            response.setResponseContent(String.format(
                "Trip cancelled successfully. Trip ID: %s. %d passenger ride(s) cancelled.",
                trip.getTripId(), cancelledRidesCount
            ));
            
        } catch (Exception e) {
            log.error("Error cancelling trip", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to cancel trip: " + e.getMessage());
        }
        
        return response;
    }
}
