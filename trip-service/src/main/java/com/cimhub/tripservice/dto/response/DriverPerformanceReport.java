package com.cimhub.tripservice.dto.response;

import lombok.Builder;

@Builder
public record DriverPerformanceReport(
        long totalDrivers,
        long totalTripsCompleted,
        long totalTripsCancelled,
        long totalTripsInProgress,
        long totalTripsAvailable) {}
