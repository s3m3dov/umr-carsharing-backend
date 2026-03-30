package com.cimhub.common.repository;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(EntityType entityType, String entityId);
    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(EntityType entityType);
}
