package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.service.routes.OSMRoute;
import com.cimhub.tripservice.dto.OSRM.OsrmResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferRideService {

        private final TripsRepository tripsRepository;
        private final OSMRoute osmRoute;
        private final TripGeospatialService tripGeospatialService;

        public OfferRideResponse offerRide(OfferRideRequest request) {
                log.info("Offering ride for driver: {} with vehicle: {}", request.driverId(), request.vehicleNumber());

                OsrmResponseDTO routeDetails = osmRoute.getRoute(
                                request.sourceAddress().getLatitude(),
                                request.sourceAddress().getLongitude(),
                                request.destinationAddress().getLatitude(),
                                request.destinationAddress().getLongitude())
                                .block();

                if (routeDetails == null || routeDetails.routes().isEmpty()) {
                        return OfferRideResponse.builder()
                                        .tripCreated(false)
                                        .errorMessage("Could not calculate route for provided coordinates")
                                        .build();
                }

                var mainRoute = routeDetails.routes().get(0);
                String timezone = TimezoneConversionService.getTimeZoneId(
                                request.sourceAddress().getLatitude(),
                                request.sourceAddress().getLongitude());

                Trip newTrip = new Trip();
                newTrip.setDriverId(request.driverId());
                newTrip.setVehicleNumber(request.vehicleNumber());
                newTrip.setSourceAddress(request.sourceAddress());
                newTrip.setDestinationAddress(request.destinationAddress());
                newTrip.setTripStartDateTimeUTC(request.tripStartDateTime().toInstant());
                newTrip.setTripTimezone(timezone);
                newTrip.setTotalSeats(request.totalSeats());
                newTrip.setBookedSeats(0);
                newTrip.setTripStatus(TripStatus.AVAILABLE);

                tripGeospatialService.enrichTripWithGeospatialData(newTrip, mainRoute);

                tripsRepository.save(newTrip);
                log.info("Trip created successfully with ID: {}", newTrip.getTripId());

                return OfferRideResponse.builder()
                                .tripId(newTrip.getTripId())
                                .vehicleNumber(newTrip.getVehicleNumber())
                                .sourceAddress(newTrip.getSourceAddress())
                                .destinationAddress(newTrip.getDestinationAddress())
                                .tripStartDateTime(request.tripStartDateTime())
                                .tripTimezone(timezone)
                                .routeGeometry(mainRoute.geometry())
                                .routeDistanceInMeters(mainRoute.distance())
                                .routeDistanceInKm(mainRoute.distance() / 1000.0)
                                .routeDurationInSeconds(mainRoute.duration())
                                .routeDurationInMinutes(mainRoute.duration() / 60.0)
                                .tripCreated(true)
                                .build();
        }
}
