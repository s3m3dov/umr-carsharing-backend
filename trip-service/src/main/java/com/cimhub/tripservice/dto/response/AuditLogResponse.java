package com.cimhub.tripservice.dto.response;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record AuditLogResponse(
        String id,
        AuditAction action,
        EntityType entityType,
        String entityId,
        String performedBy,
        String details,
        String serviceName,
        Instant timestamp
) {
}
