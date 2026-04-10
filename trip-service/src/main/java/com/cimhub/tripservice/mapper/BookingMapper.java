package com.cimhub.tripservice.mapper;

import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

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
                .estimatedPrice(b.getEstimatedPrice())
                .pricePerSeat(b.getPricePerSeat())
                .rideStartTimeUTC(b.getRideStartTimeUTC())
                .rideTimezone(b.getRideTimezone())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
