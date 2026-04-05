package com.cimhub.tripservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    /**
     * Get booking details by ID.
     * Returns public booking information for review validation and other purposes.
     *
     * @param bookingId the booking ID
     * @return BookingResponse with booking details
     * @throws ResourceNotFoundException if booking is not found
     */
    public BookingResponse getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking b) {
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
