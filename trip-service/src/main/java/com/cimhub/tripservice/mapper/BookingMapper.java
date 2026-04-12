package com.cimhub.tripservice.mapper;

import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.dto.response.PassengerRideResponse;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final TimezoneConversionService timezoneService;

    public BookingResponse toBookingResponse(Booking b) {
        return BookingResponse.builder()
                .bookingId(b.getBookingId())
                .passengerId(b.getPassengerId())
                .tripId(b.getTripId())
                .driverId(b.getDriverId())
                .vehicleNumber(b.getVehicleNumber())
                .pickupLocation(b.getPickupLocation())
                .dropoffLocation(b.getDropoffLocation())
                .requestedSeats(b.getRequestedSeats())
                .status(b.getStatus())
                .pricePerSeat(b.getPricePerSeat())
                .rideStartTimeUTC(b.getRideStartTimeUTC())
                .rideTimezone(b.getRideTimezone())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    /**
     * Converts a Booking + its parent Trip to a passenger-facing ride response.
     * Trip may be null if it has been deleted; car type will be omitted in that case.
     */
    public PassengerRideResponse toPassengerRideResponse(Booking booking, Trip trip) {
        ZonedDateTime rideStartInOriginalZone =
                timezoneService.convertToTimezone(booking.getRideStartTimeUTC(), booking.getRideTimezone());

        String carType = trip != null ? trip.getCarType() : null;

        return PassengerRideResponse.builder()
                .rideId(booking.getBookingId())
                .tripId(booking.getTripId())
                .driverId(booking.getDriverId())
                .vehicleNumber(booking.getVehicleNumber())
                .carType(carType)
                .rideStatus(booking.getStatus())
                .pickupLocation(booking.getPickupLocation())
                .dropoffLocation(booking.getDropoffLocation())
                .tripStartDateTime(rideStartInOriginalZone)
                .tripTimezone(booking.getRideTimezone())
                .bookedSeats(booking.getRequestedSeats())
                .routeGeometry(trip != null ? trip.getRouteGeometry() : null)
                .pricePerSeat(booking.getPricePerSeat())
                .build();
    }
}
