package com.cimhub.tripservice.service.booking;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.tripservice.dto.response.BookingResponse;
import com.cimhub.tripservice.mapper.BookingMapper;
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
    private final BookingMapper bookingMapper;

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
        return bookingMapper.toBookingResponse(booking);
    }
}
