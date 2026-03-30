package com.cimhub.tripservice.dto.response;

import lombok.Builder;

@Builder
public record RevenueReport(
        long totalCompletedBookings,
        double totalRevenue,
        double averageRevenuePerBooking) {
}
