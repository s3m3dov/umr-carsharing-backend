package com.singhv.tripservice.service.impl;
import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.OSRM.OsrmResponseDTO;
import com.singhv.tripservice.dto.OSRM.Route;
import com.singhv.tripservice.dto.OfferRideRequestDTO;
import com.singhv.tripservice.dto.OfferRideResponseDTO;
import com.singhv.tripservice.model.Trips;
import com.singhv.tripservice.repository.TripsRepository;
import com.singhv.tripservice.service.OfferRideService;
import com.singhv.tripservice.service.routes.OSMRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferRideServiceImpl implements OfferRideService {

    private final TripsRepository tripsRepository;
    private final OSMRoute osmRoute;

    @Override
    public ResponseDTO<OfferRideResponseDTO> offerRide(RequestDTO<OfferRideRequestDTO> offerRideRequest) {

        OfferRideRequestDTO requestContent = offerRideRequest.getRequestContent();
        OfferRideResponseDTO responseContent = new OfferRideResponseDTO();
        ResponseDTO<OfferRideResponseDTO> responseDTO = new ResponseDTO<>();

        // Convert the ZonedDateTime to UTC Instant for storage
        Instant tripStartInstant = requestContent.getTripStartDateTime().toInstant();
        String tripTimezone = requestContent.getTripStartDateTime().getZone().getId();

        // Check if the driver already has a trip at overlapping time
        // We check for trips within +/- 12 hours to handle timezone overlaps
        Instant rangeStart = tripStartInstant.minus(12, ChronoUnit.HOURS);
        Instant rangeEnd = tripStartInstant.plus(12, ChronoUnit.HOURS);

        List<Trips> existingTrips = tripsRepository.findByDriverIdAndTripStartDateTimeUTCBetween(
                offerRideRequest.getUserId(),
                rangeStart,
                rangeEnd
        );

        // More precise check: trips within 1 hour window
        boolean hasConflict = existingTrips.stream()
                .anyMatch(trip -> {
                    long hoursDiff = Math.abs(ChronoUnit.HOURS.between(trip.getTripStartDateTimeUTC(), tripStartInstant));
                    return hoursDiff < 1;
                });

        if (hasConflict) {
            responseContent.setErrorMessage("Driver already has a trip at the same time. Please choose a different time.");
            responseContent.setTripCreated(false);
            responseDTO.setResponseContent(responseContent);
            return responseDTO;
        }

        // Get route from OSRM API
        OsrmResponseDTO osrmResponse;
        try {
            osrmResponse = osmRoute.getRoute(
                    requestContent.getSourceAddress().getLatitude(),
                    requestContent.getSourceAddress().getLongitude(),
                    requestContent.getDestinationAddress().getLatitude(),
                    requestContent.getDestinationAddress().getLongitude()
            ).block(); // Block to make it synchronous

            if (osrmResponse == null || osrmResponse.getRoutes() == null || osrmResponse.getRoutes().isEmpty()) {
                log.error("No route found from OSRM API");
                responseContent.setErrorMessage("Could not find a route between source and destination. Please check the addresses.");
                responseContent.setTripCreated(false);
                responseDTO.setResponseContent(responseContent);
                return responseDTO;
            }
        } catch (Exception e) {
            log.error("Error calling OSRM API: ", e);
            responseContent.setErrorMessage("Failed to calculate route. Please try again later.");
            responseContent.setTripCreated(false);
            responseDTO.setResponseContent(responseContent);
            return responseDTO;
        }

        // Extract route information
        Route route = osrmResponse.getRoutes().get(0);

        // Create GeoJSON Points for geospatial queries
        // MongoDB GeoJSON format: [longitude, latitude]
        Point sourceLocation = new Point(
                requestContent.getSourceAddress().getLongitude(),
                requestContent.getSourceAddress().getLatitude()
        );

        Point destinationLocation = new Point(
                requestContent.getDestinationAddress().getLongitude(),
                requestContent.getDestinationAddress().getLatitude()
        );

        // Create a new trip - store in UTC for universal consistency
        Trips newTrip = Trips.builder()
                .driverId(offerRideRequest.getUserId())
                .tripStatus("OFFERED")
                .vehicleNumber(requestContent.getVehicleNumber())
                .sourceAddress(requestContent.getSourceAddress())
                .destinationAddress(requestContent.getDestinationAddress())
                .sourceLocation(sourceLocation)  // GeoJSON Point for geospatial queries
                .destinationLocation(destinationLocation)  // GeoJSON Point for geospatial queries
                .offeredSeat(requestContent.getOfferedSeat())
                .currSeats(0)
                .tripStartDateTimeUTC(tripStartInstant)  // Store in UTC
                .tripTimezone(tripTimezone)  // Store original timezone for reference
                .routeGeometry(route.getGeometry())  // Store GeoJSON LineString
                .routeDistance(route.getDistance())  // Distance in meters
                .routeDuration(route.getDuration())  // Duration in seconds
                .pricePerKm(10.0)
                .build();

        Trips savedTrip = tripsRepository.save(newTrip);

        // Build response with timezone information and route data
        responseContent.setTripId(savedTrip.getTripId());
        responseContent.setVehicleNumber(savedTrip.getVehicleNumber());
        responseContent.setSourceAddress(savedTrip.getSourceAddress());
        responseContent.setDestinationAddress(savedTrip.getDestinationAddress());
        responseContent.setTripStartDateTime(requestContent.getTripStartDateTime());
        responseContent.setTripTimezone(tripTimezone);

        // Add route information to response
        responseContent.setRouteGeometry(savedTrip.getRouteGeometry());
        responseContent.setRouteDistanceInMeters(savedTrip.getRouteDistance());
        responseContent.setRouteDistanceInKm(savedTrip.getRouteDistance() / 1000.0); // Convert meters to km
        responseContent.setRouteDurationInSeconds(savedTrip.getRouteDuration());
        responseContent.setRouteDurationInMinutes(savedTrip.getRouteDuration() / 60.0); // Convert seconds to minutes

        responseContent.setTripCreated(true);

        responseDTO.setResponseContent(responseContent);

        log.info("Trip created successfully. Distance: {} km, Duration: {} minutes",
                 savedTrip.getRouteDistance() / 1000.0, savedTrip.getRouteDuration() / 60.0);

        return responseDTO;
    }

}
