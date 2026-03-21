package com.mindtrack.audit.service;

import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.model.AuditLog;
import com.mindtrack.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    AuditLogRepository auditLogRepository;

    @InjectMocks
    AuditService auditService;

    @Test
    void shouldSaveAuditLog() {
        auditService.log(1L, AuditAction.READ, "INTERVIEW", 42L, 1L, "127.0.0.1", "WEB");
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals(AuditAction.READ, captor.getValue().getAction());
        assertEquals("INTERVIEW", captor.getValue().getResourceType());
        assertEquals(42L, captor.getValue().getResourceId());
        assertEquals(1L, captor.getValue().getActorUserId());
        assertEquals(1L, captor.getValue().getPatientUserId());
        assertEquals("127.0.0.1", captor.getValue().getIpAddress());
        assertEquals("WEB", captor.getValue().getChannel());
    }

    @Test
    void shouldSwallowRepositoryErrors() {
        doThrow(new RuntimeException("db down")).when(auditLogRepository).save(org.mockito.ArgumentMatchers.any(AuditLog.class));

        assertDoesNotThrow(() ->
                auditService.log(2L, AuditAction.WRITE, "PROFILE", 9L, 4L, "10.0.0.1", "API"));
    }
}
