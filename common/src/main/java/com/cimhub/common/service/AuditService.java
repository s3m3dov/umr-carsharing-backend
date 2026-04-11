package com.cimhub.common.service;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.model.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Centralized audit logging service for tracking all operations (admin, user, system).
 * Use this service for all auditable operations to maintain a consistent audit trail.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final MongoTemplate mongoTemplate;

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    /**
     * Log an action with full details.
     *
     * @param action Action performed
     * @param entityType Type of entity
     * @param entityId ID of the entity
     * @param performedBy Email/ID of who performed the action
     * @param details Additional context or reason
     */
    private void logAction(
            AuditAction action, EntityType entityType, String entityId, String performedBy, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .performedBy(performedBy)
                    .details(details)
                    .serviceName(serviceName)
                    .build();

            mongoTemplate.save(auditLog);
            log.info(
                    "Audit: {} {} by {} on {}:{} - {}", action, entityType, performedBy, entityType, entityId, details);
        } catch (Exception e) {
            // Don't fail the operation if audit logging fails
            log.error("Failed to save audit log: {} {} on {}:{}", action, entityType, entityType, entityId, e);
        }
    }

    /**
     * Log an action without additional details
     */
    private void logAction(AuditAction action, EntityType entityType, String entityId, String performedBy) {
        logAction(action, entityType, entityId, performedBy, null);
    }

    /**
     * Log a CREATE action
     */
    public void logCreate(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.CREATE, entityType, entityId, performedBy, details);
    }

    /**
     * Log an UPDATE action
     */
    public void logUpdate(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.UPDATE, entityType, entityId, performedBy, details);
    }

    /**
     * Log a DELETE action
     */
    public void logDelete(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.DELETE, entityType, entityId, performedBy, details);
    }

    /**
     * Log an APPROVE action (for driver approvals, etc.)
     */
    public void logApprove(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.APPROVE, entityType, entityId, performedBy, details);
    }

    /**
     * Log a REJECT action (for driver rejections, etc.)
     */
    public void logReject(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.REJECT, entityType, entityId, performedBy, details);
    }

    /**
     * Log a FLAG action (for review flagging, etc.)
     */
    public void logFlag(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.FLAG, entityType, entityId, performedBy, details);
    }

    /**
     * Log a CANCEL action (for trip/booking cancellations, etc.)
     */
    public void logCancel(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.CANCEL, entityType, entityId, performedBy, details);
    }

    /**
     * Log a BOOK action (for bookings)
     */
    public void logBook(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.BOOK, entityType, entityId, performedBy, details);
    }

    /**
     * Log a COMPLETE action (for trip/ride completions)
     */
    public void logComplete(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.COMPLETE, entityType, entityId, performedBy, details);
    }

    /**
     * Log a PUBLISH action (for reviews, etc.)
     */
    public void logPublish(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.PUBLISH, entityType, entityId, performedBy, details);
    }

    /**
     * Log a LOGIN action (for user authentication events)
     */
    public void logLogin(EntityType entityType, String entityId, String performedBy, String details) {
        logAction(AuditAction.LOGIN, entityType, entityId, performedBy, details);
    }
}
