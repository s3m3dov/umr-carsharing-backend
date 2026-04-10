package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.mapper.BookingMapper;
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
    private final AuditService auditService;
    private final BookingMapper bookingMapper;

    public Page<BookingResponse> listBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toBookingResponse);
    }

    public BookingResponse getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return bookingMapper.toBookingResponse(booking);
    }

    public BookingResponse updateBooking(String bookingId, UpdateBookingRequest request, String adminEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (request.status() != null) booking.setStatus(request.status());
        if (request.requestedSeats() != null) booking.setRequestedSeats(request.requestedSeats());
        booking.setUpdatedAt(Instant.now());

        bookingRepository.save(booking);
        auditService.logUpdate(EntityType.BOOKING, bookingId, adminEmail, "Booking updated by admin");
        log.info("Booking {} updated by admin {}", bookingId, adminEmail);
        return bookingMapper.toBookingResponse(booking);
    }

    public void cancelBooking(String bookingId, String adminEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        auditService.logCancel(EntityType.BOOKING, bookingId, adminEmail, "Booking cancelled by admin");
        log.info("Booking {} cancelled by admin {}", bookingId, adminEmail);
    }

    public Page<BookingResponse> getPassengerBookings(String passengerId, Pageable pageable) {
        return bookingRepository.findByPassengerId(passengerId, pageable).map(bookingMapper::toBookingResponse);
    }
}
