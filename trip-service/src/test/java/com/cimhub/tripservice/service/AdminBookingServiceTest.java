package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.mapper.BookingMapper;
import com.cimhub.tripservice.service.booking.AdminBookingService;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuditService auditService;

    private AdminBookingService adminBookingService;

    private BookingMapper bookingMapper;

    @BeforeEach
    void setUp() {
        bookingMapper = new BookingMapper(new TimezoneConversionService());
        adminBookingService = new AdminBookingService(bookingRepository, auditService, bookingMapper);
    }

    @Test
    void updateBooking_shouldApplyChangesAndAudit() {
        Booking booking = new Booking();
        booking.setBookingId("booking-1");
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setRequestedSeats(1);

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = adminBookingService.updateBooking(
                "booking-1",
                new UpdateBookingRequest(BookingStatus.CONFIRMED, 2),
                "admin@example.com"
        );

        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(2, response.requestedSeats());

        verify(auditService).logUpdate(EntityType.BOOKING, "booking-1", "admin@example.com", "Booking updated by admin");
    }

    @Test
    void cancelBooking_shouldMarkCancelledAndAudit() {
        Booking booking = new Booking();
        booking.setBookingId("booking-2");
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById("booking-2")).thenReturn(Optional.of(booking));

        adminBookingService.cancelBooking("booking-2", "admin@example.com");

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals(BookingStatus.CANCELLED, bookingCaptor.getValue().getStatus());

        verify(auditService).logCancel(EntityType.BOOKING, "booking-2", "admin@example.com", "Booking cancelled by admin");
    }

    @Test
    void getBooking_whenMissing_shouldThrowResourceNotFound() {
        when(bookingRepository.findById("missing-booking")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminBookingService.getBooking("missing-booking"));
    }

    @Test
    void getBooking_shouldReturnMappedResponse() {
        Booking booking = new Booking();
        booking.setBookingId("booking-1");
        booking.setPassengerId("passenger-1");
        booking.setTripId("trip-1");
        booking.setDriverId("driver-1");
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setRequestedSeats(2);

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        BookingResponse response = adminBookingService.getBooking("booking-1");

        assertEquals("booking-1", response.bookingId());
        assertEquals("passenger-1", response.passengerId());
        assertEquals("trip-1", response.tripId());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(2, response.requestedSeats());
    }

    @Test
    void updateBooking_whenNotFound_shouldThrowResourceNotFound() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminBookingService.updateBooking("missing",
                        new UpdateBookingRequest(BookingStatus.CONFIRMED, 1), "admin@example.com"));
    }

    @Test
    void updateBooking_withOnlyStatusSet_shouldPreserveExistingSeats() {
        Booking booking = new Booking();
        booking.setBookingId("booking-1");
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setRequestedSeats(3);

        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = adminBookingService.updateBooking(
                "booking-1", new UpdateBookingRequest(BookingStatus.CONFIRMED, null), "admin@example.com");

        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(3, response.requestedSeats());
    }

    @Test
    void cancelBooking_whenNotFound_shouldThrowResourceNotFound() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminBookingService.cancelBooking("missing", "admin@example.com"));
    }

    @Test
    void updateBooking_withOnlyRequestedSeatsSet_shouldPreserveExistingStatus() {
        Booking booking = new Booking();
        booking.setBookingId("booking-3");
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setRequestedSeats(1);

        when(bookingRepository.findById("booking-3")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = adminBookingService.updateBooking(
                "booking-3", new UpdateBookingRequest(null, 4), "admin@example.com");

        assertEquals(BookingStatus.REQUESTED, response.status());
        assertEquals(4, response.requestedSeats());
    }

    @Test
    void listBookings_and_getPassengerBookings_shouldReturnMappedPages() {
        Pageable pageable = PageRequest.of(0, 10);
        Booking booking = new Booking();
        booking.setBookingId("booking-9");
        booking.setPassengerId("passenger-9");
        booking.setTripId("trip-9");
        booking.setDriverId("driver-9");
        booking.setVehicleNumber("HE-XX-9999");
        booking.setRequestedSeats(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setEstimatedPrice(25.0);
        booking.setPricePerSeat(12.5);
        booking.setRideStartTimeUTC(Instant.parse("2026-09-01T10:00:00Z"));
        booking.setRideTimezone("UTC");
        booking.setCreatedAt(Instant.parse("2026-09-01T09:00:00Z"));
        booking.setUpdatedAt(Instant.parse("2026-09-01T09:30:00Z"));

        Page<Booking> page = new PageImpl<>(List.of(booking), pageable, 1);
        when(bookingRepository.findAll(pageable)).thenReturn(page);
        when(bookingRepository.findByPassengerId("passenger-9", pageable)).thenReturn(page);

        Page<BookingResponse> listed = adminBookingService.listBookings(pageable);
        Page<BookingResponse> byPassenger = adminBookingService.getPassengerBookings("passenger-9", pageable);

        assertEquals(1, listed.getTotalElements());
        assertEquals("booking-9", listed.getContent().getFirst().bookingId());
        assertEquals("driver-9", listed.getContent().getFirst().driverId());
        assertEquals(25.0, listed.getContent().getFirst().estimatedPrice());

        assertEquals(1, byPassenger.getTotalElements());
        assertEquals("passenger-9", byPassenger.getContent().getFirst().passengerId());
    }
}
