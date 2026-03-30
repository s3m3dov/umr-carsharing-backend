package com.cimhub.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkDriverActionRequest(
        @NotEmpty(message = "Driver IDs are required") List<String> driverIds) {
}
