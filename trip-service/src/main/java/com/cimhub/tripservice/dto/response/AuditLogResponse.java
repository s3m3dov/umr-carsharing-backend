package com.cimhub.tripservice.dto.response;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record AuditLogResponse(
        String id,
        AuditAction action,
        EntityType entityType,
        String entityId,
        String performedBy,
        String details,
        String serviceName,
        Instant timestamp) {}
