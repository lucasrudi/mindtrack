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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
