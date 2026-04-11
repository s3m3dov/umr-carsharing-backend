package com.cimhub.tripservice.mapper;

import com.cimhub.tripservice.dto.response.AdminTripResponse;
import com.cimhub.tripservice.dto.response.DriverTripResponse;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TripMapper {

    private final TimezoneConversionService timezoneService;

    public TripResponse toTripResponse(Trip trip) {
        String timezone =
                (trip.getTripTimezone() == null || trip.getTripTimezone().isBlank()) ? "UTC" : trip.getTripTimezone();

        ZonedDateTime tripStartDateTime = trip.getTripStartDateTimeUTC() != null
                ? ZonedDateTime.ofInstant(trip.getTripStartDateTimeUTC(), ZoneId.of(timezone))
                : null;

        return TripResponse.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .carType(trip.getCarType())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .tripStartDateTime(tripStartDateTime)
                .tripTimezone(timezone)
                .tripStartDateTimeInUserTimezone(tripStartDateTime)
                .userTimezone(timezone)
                .totalSeats(trip.getTotalSeats())
                .availableSeats(trip.getTotalSeats() - trip.getBookedSeats())
                .tripStatus(trip.getTripStatus())
                .pricePerSeat(trip.getPricePerSeat())
                .build();
    }

    public AdminTripResponse toAdminTripResponse(Trip trip) {
        return AdminTripResponse.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .carType(trip.getCarType())
                .tripStatus(trip.getTripStatus())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .totalSeats(trip.getTotalSeats())
                .bookedSeats(trip.getBookedSeats())
                .availableSeats(trip.getTotalSeats() - trip.getBookedSeats())
                .pricePerSeat(trip.getPricePerSeat())
                .routeDistance(trip.getRouteDistance())
                .routeDuration(trip.getRouteDuration())
                .routeGeometry(trip.getRouteGeometry())
                .tripStartDateTimeUTC(trip.getTripStartDateTimeUTC())
                .tripTimezone(trip.getTripTimezone())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    /**
     * Converts a Trip to TripResponse with times shown in both the trip's
     * original timezone and the caller-supplied user timezone.
     */
    public TripResponse toTripResponse(Trip trip, String userTimezone) {
        String tripTimezone =
                (trip.getTripTimezone() == null || trip.getTripTimezone().isBlank()) ? "UTC" : trip.getTripTimezone();

        ZonedDateTime tripTimeInOriginalZone = trip.getTripStartDateTimeUTC() != null
                ? timezoneService.convertToTripTimezone(trip.getTripStartDateTimeUTC(), tripTimezone)
                : null;

        ZonedDateTime tripTimeInUserZone = trip.getTripStartDateTimeUTC() != null
                ? timezoneService.convertToUserTimezone(trip.getTripStartDateTimeUTC(), userTimezone)
                : null;

        return TripResponse.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .carType(trip.getCarType())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .tripStartDateTime(tripTimeInOriginalZone)
                .tripTimezone(tripTimezone)
                .tripStartDateTimeInUserTimezone(tripTimeInUserZone)
                .userTimezone(userTimezone)
                .totalSeats(trip.getTotalSeats())
                .availableSeats(trip.getTotalSeats() - trip.getBookedSeats())
                .tripStatus(trip.getTripStatus())
                .pricePerSeat(trip.getPricePerSeat())
                .build();
    }

    public DriverTripResponse toDriverTripResponse(Trip trip) {
        ZonedDateTime tripTimeInOriginalZone =
                timezoneService.convertToTripTimezone(trip.getTripStartDateTimeUTC(), trip.getTripTimezone());

        int bookedSeats = trip.getBookedSeats();
        int availableSeats = trip.getTotalSeats() - bookedSeats;
        double estimatedEarnings = bookedSeats * trip.getPricePerSeat();

        return DriverTripResponse.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .vehicleNumber(trip.getVehicleNumber())
                .carType(trip.getCarType())
                .tripStatus(trip.getTripStatus())
                .sourceAddress(trip.getSourceAddress())
                .destinationAddress(trip.getDestinationAddress())
                .tripStartDateTime(tripTimeInOriginalZone)
                .tripTimezone(trip.getTripTimezone())
                .totalSeats(trip.getTotalSeats())
                .availableSeats(availableSeats)
                .bookedSeats(bookedSeats)
                .passengers(new ArrayList<>())
                .routeDistanceInKm(trip.getRouteDistance() / 1000.0)
                .routeDurationInMinutes(trip.getRouteDuration() / 60.0)
                .routeGeometry(trip.getRouteGeometry())
                .pricePerSeat(trip.getPricePerSeat())
                .estimatedEarnings(estimatedEarnings)
                .build();
    }
}
