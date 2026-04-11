package com.cimhub.userservice.dto.response;

import java.time.Instant;
import lombok.Builder;

@Builder
public record DriverDocumentResponse(
        String id,
        String driverId,
        String documentType,
        String fileName,
        String fileUrl,
        long fileSize,
        Instant uploadedAt) {}
