package com.cimhub.tripservice.dto.response;

import lombok.Builder;

@Builder
public record BookingSummaryReport(
        long totalBookings,
        long confirmedBookings,
        long cancelledBookings,
        long requestedBookings,
        long completedBookings,
        long rejectedBookings) {}
