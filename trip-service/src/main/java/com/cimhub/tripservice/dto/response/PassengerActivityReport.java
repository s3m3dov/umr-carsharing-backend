package com.cimhub.tripservice.dto.response;

import lombok.Builder;

@Builder
public record PassengerActivityReport(
        long totalBookings,
        long activeBookings,
        long completedBookings,
        long cancelledBookings) {
}
