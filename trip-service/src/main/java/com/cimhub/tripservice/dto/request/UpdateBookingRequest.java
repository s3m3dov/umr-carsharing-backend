package com.cimhub.tripservice.dto.request;

import com.cimhub.common.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
@Schema(
        name = "UpdateBookingRequest",
        description = "Request to update a booking (status or seat count)"
)
public record UpdateBookingRequest(
        @Schema(description = "New booking status", example = "CONFIRMED")
        BookingStatus status,

        @Schema(description = "Updated number of seats requested", example = "2")
        @Min(value = 1, message = "At least 1 seat must be requested")
        @Max(value = 8, message = "Maximum 8 seats allowed")
        Integer requestedSeats) {
}