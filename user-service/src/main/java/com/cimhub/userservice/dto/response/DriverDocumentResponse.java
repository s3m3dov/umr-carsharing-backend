package com.cimhub.userservice.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record DriverDocumentResponse(
        String id,
        String driverId,
        String documentType,
        String fileName,
        String fileUrl,
        long fileSize,
        Instant uploadedAt) {
}
