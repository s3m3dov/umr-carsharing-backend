package com.cimhub.tripservice.dto.request;

import com.cimhub.tripservice.model.BookingStatus;

public record UpdateBookingRequest(
        BookingStatus status,
        Integer requestedSeats) {
}
