package com.cimhub.common.model;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Audit log for tracking all operations across the platform.
 * Centralized logging of admin, user, and system actions for compliance and monitoring.
 */
@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    private AuditAction action; // Action performed (CREATE, UPDATE, DELETE, etc.)
    private EntityType entityType; // Type of entity affected (DRIVER, TRIP, BOOKING, etc.)
    private String entityId; // ID of the affected entity
    private String performedBy; // Email/ID of who performed the action (admin, user, or "SYSTEM")
    private String details; // Additional context or reason
    private String serviceName; // Which service performed the action

    @Builder.Default
    private Instant timestamp = Instant.now();
}
