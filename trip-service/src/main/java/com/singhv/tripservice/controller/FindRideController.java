package com.singhv.tripservice.controller;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.FindRideRequestDTO;
import com.singhv.tripservice.model.Trips;
import com.singhv.tripservice.service.TripGeospatialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for finding available rides based on user's travel requirements
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Slf4j
public class FindRideController {

    private final TripGeospatialService geoService;

    /**
     * Find available rides matching the user's source and destination
     *
     * @param request Contains pickup and dropoff location details with search radius
     * @return List of matching trips
     */
    @PostMapping("/find-ride")
    public ResponseEntity<ResponseDTO<List<Trips>>> findRide(
            @RequestBody RequestDTO<FindRideRequestDTO> request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        FindRideRequestDTO rideRequest = request.getRequestContent();

        // Use userId from header if available, otherwise from request body
        String effectiveUserId = userId != null ? userId : request.getUserId();

        // Validate required fields
        if (rideRequest.getPickupPoint() == null || rideRequest.getDestinationPoint() == null) {
            log.error("Missing required pickup or destination point");
            ResponseDTO<List<Trips>> errorResponse = new ResponseDTO<>();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Pickup point and destination point are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Double pickupLat = rideRequest.getPickupPoint().getLatitude();
        Double pickupLon = rideRequest.getPickupPoint().getLongitude();
        Double destLat = rideRequest.getDestinationPoint().getLatitude();
        Double destLon = rideRequest.getDestinationPoint().getLongitude();
        
        log.info("Finding rides for user: {} from ({}, {}) to ({}, {})",
                effectiveUserId,
                pickupLat, pickupLon, destLat, destLon);

        // Default radius to 5km for geospatial search
        double sourceRadius = 5.0;
        double destRadius = 5.0;
        
         String rideStartTime = rideRequest.getRideStartTime();
        Integer requestedSeats = rideRequest.getRequestedSeats();
        List<Trips> matchingTrips = geoService.findTripsMatchingRoute(
                pickupLat, pickupLon, sourceRadius,
                destLat, destLon, destRadius, rideStartTime, requestedSeats, effectiveUserId
        );
        log.info("Found {} matching trips for user: {}", matchingTrips.size(), effectiveUserId);

        ResponseDTO<List<Trips>> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setResponseContent(matchingTrips);

        return ResponseEntity.ok(response);
    }
}
