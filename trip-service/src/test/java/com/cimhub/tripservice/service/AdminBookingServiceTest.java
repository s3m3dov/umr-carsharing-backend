package com.cimhub.tripservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.tripservice.dto.request.UpdateBookingRequest;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        adminBookingService = new AdminBookingService(bookingRepository, auditService);
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
}
