package com.mindtrack.audit.repository;

import com.mindtrack.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for audit log persistence.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
