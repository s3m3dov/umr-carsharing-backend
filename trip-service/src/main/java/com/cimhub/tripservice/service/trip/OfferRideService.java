package com.cimhub.tripservice.service.trip;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.OfferRideRequest;
import com.cimhub.tripservice.dto.response.OfferRideResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.model.VehicleDocument;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.repository.VehicleDocumentRepository;
import com.cimhub.tripservice.service.geo.TripGeospatialService;
import com.cimhub.tripservice.service.routes.OsrmRouteClient;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import com.cimhub.tripservice.dto.osrm.OsrmResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferRideService {

        private final TripsRepository tripsRepository;
        private final VehicleDocumentRepository vehicleDocumentRepository;
        private final OsrmRouteClient osmRoute;
        private final TripGeospatialService tripGeospatialService;
        private final AuditService auditService;

        public OfferRideResponse offerRide(OfferRideRequest request) {
                log.info("Offering ride for driver: {}", request.driverId());

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

                VehicleDocument vehicle = vehicleDocumentRepository
                                .findByUserId(request.driverId())
                                .orElse(null);
                if (vehicle == null) {
                        return OfferRideResponse.builder()
                                        .tripCreated(false)
                                        .errorMessage("Vehicle not found or does not belong to driver")
                                        .build();
                }

                var mainRoute = routeDetails.routes().get(0);
                String timezone = TimezoneConversionService.getTimeZoneId(
                                request.sourceAddress().getLatitude(),
                                request.sourceAddress().getLongitude());

                Trip newTrip = new Trip();
                newTrip.setDriverId(request.driverId());
                newTrip.setVehicleNumber(vehicle.getVehicleNumber());
                newTrip.setCarType(vehicle.getVehicleType());
                newTrip.setSourceAddress(request.sourceAddress());
                newTrip.setDestinationAddress(request.destinationAddress());

                // Truncate to seconds to ignore milliseconds
                ZonedDateTime truncatedStartTime = request.tripStartDateTime().truncatedTo(ChronoUnit.SECONDS);
                newTrip.setTripStartDateTimeUTC(truncatedStartTime.toInstant());

                newTrip.setTripTimezone(timezone);
                newTrip.setTotalSeats(request.totalSeats());
                newTrip.setBookedSeats(0);
                newTrip.setPricePerSeat(request.pricePerSeat());
                newTrip.setTripStatus(TripStatus.AVAILABLE);

                tripGeospatialService.enrichTripWithGeospatialData(newTrip, mainRoute);

                tripsRepository.save(newTrip);
                auditService.logCreate(EntityType.TRIP, newTrip.getTripId(), request.driverId(), "Trip created by driver");
                log.info("Trip created successfully with ID: {}", newTrip.getTripId());

                return OfferRideResponse.builder()
                                .tripId(newTrip.getTripId())
                                .vehicleNumber(newTrip.getVehicleNumber())
                                .sourceAddress(newTrip.getSourceAddress())
                                .destinationAddress(newTrip.getDestinationAddress())
                                .tripStartDateTime(truncatedStartTime)
                                .tripTimezone(timezone)
                                .routeGeometry(newTrip.getRouteGeometry())
                                .routeDistanceInMeters(mainRoute.distance())
                                .routeDistanceInKm(mainRoute.distance() / 1000.0)
                                .routeDurationInSeconds(mainRoute.duration())
                                .routeDurationInMinutes(mainRoute.duration() / 60.0)
                                .pricePerSeat(newTrip.getPricePerSeat())
                                .tripStatus(newTrip.getTripStatus())
                                .tripCreated(true)
                                .build();
        }
}
