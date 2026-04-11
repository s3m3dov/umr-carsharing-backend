package com.cimhub.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cimhub.common.enums.AuditAction;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.model.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(mongoTemplate);
        ReflectionTestUtils.setField(auditService, "serviceName", "test-service");
        lenient().when(mongoTemplate.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void logCreate_shouldPersistAuditLogWithExpectedFields() {
        auditService.logCreate(EntityType.USER, "u-1", "admin", "Created user");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(mongoTemplate).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(AuditAction.CREATE, saved.getAction());
        assertEquals(EntityType.USER, saved.getEntityType());
        assertEquals("u-1", saved.getEntityId());
        assertEquals("admin", saved.getPerformedBy());
        assertEquals("Created user", saved.getDetails());
        assertEquals("test-service", saved.getServiceName());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    void logMethods_shouldMapToCorrectActions() {
        auditService.logUpdate(EntityType.USER, "u-1", "admin", "Updated user");
        auditService.logDelete(EntityType.USER, "u-1", "admin", "Deleted user");
        auditService.logApprove(EntityType.DRIVER, "d-1", "admin", "Approved driver");
        auditService.logReject(EntityType.DRIVER, "d-1", "admin", "Rejected driver");
        auditService.logFlag(EntityType.REVIEW, "r-1", "admin", "Flagged review");
        auditService.logCancel(EntityType.BOOKING, "b-1", "admin", "Cancelled booking");
        auditService.logBook(EntityType.BOOKING, "b-2", "user", "Booked trip");
        auditService.logPublish(EntityType.REVIEW, "r-2", "user", "Published review");
        auditService.logLogin(EntityType.USER, "u-2", "user", "Logged in");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(mongoTemplate, times(9)).save(captor.capture());

        assertEquals(AuditAction.UPDATE, captor.getAllValues().get(0).getAction());
        assertEquals(AuditAction.DELETE, captor.getAllValues().get(1).getAction());
        assertEquals(AuditAction.APPROVE, captor.getAllValues().get(2).getAction());
        assertEquals(AuditAction.REJECT, captor.getAllValues().get(3).getAction());
        assertEquals(AuditAction.FLAG, captor.getAllValues().get(4).getAction());
        assertEquals(AuditAction.CANCEL, captor.getAllValues().get(5).getAction());
        assertEquals(AuditAction.BOOK, captor.getAllValues().get(6).getAction());
        assertEquals(AuditAction.PUBLISH, captor.getAllValues().get(7).getAction());
        assertEquals(AuditAction.LOGIN, captor.getAllValues().get(8).getAction());
    }

    @Test
    void logAction_whenMongoFails_shouldNotThrow() {
        doThrow(new RuntimeException("mongo down")).when(mongoTemplate).save(any(AuditLog.class));

        auditService.logCreate(EntityType.USER, "u-3", "admin", "Created user");

        verify(mongoTemplate).save(any(AuditLog.class));
    }
}
