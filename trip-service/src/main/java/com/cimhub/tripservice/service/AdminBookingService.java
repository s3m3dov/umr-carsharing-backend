package com.cimhub.tripservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.model.AuditLog;
import com.cimhub.common.repository.AuditLogRepository;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final AuditLogRepository auditLogRepository;

    public Page<BookingResponse> listBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::mapToResponse);
    }

    public BookingResponse getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return mapToResponse(booking);
    }

    public BookingResponse updateBooking(String bookingId, UpdateBookingRequest request, String adminEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (request.status() != null) booking.setStatus(request.status());
        if (request.requestedSeats() != null) booking.setRequestedSeats(request.requestedSeats());
        booking.setUpdatedAt(Instant.now());

        bookingRepository.save(booking);
        audit("UPDATE", "BOOKING", bookingId, adminEmail, "Booking updated by admin");
        log.info("Booking {} updated by admin {}", bookingId, adminEmail);
        return mapToResponse(booking);
    }

    public void cancelBooking(String bookingId, String adminEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        audit("CANCEL", "BOOKING", bookingId, adminEmail, "Booking cancelled by admin");
        log.info("Booking {} cancelled by admin {}", bookingId, adminEmail);
    }

    public Page<BookingResponse> getPassengerBookings(String passengerId, Pageable pageable) {
        return bookingRepository.findByPassengerId(passengerId, pageable).map(this::mapToResponse);
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

    private void audit(String action, String entityType, String entityId, String performedBy, String details) {
        auditLogRepository.save(AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .build());
    }
}
