package com.mindtrack.audit.service;

import com.mindtrack.audit.model.AuditAction;
import com.mindtrack.audit.model.AuditLog;
import com.mindtrack.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async service for recording PHI access audit logs.
 * The {@code log} method is fire-and-forget — failures are caught internally
 * so audit errors never propagate to the request thread.
 */
@Service
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records an audit log entry asynchronously.
     *
     * @param actorUserId   the ID of the user performing the action
     * @param action        the type of action performed
     * @param resourceType  the type of resource accessed
     * @param resourceId    the ID of the resource accessed
     * @param patientUserId the ID of the patient whose data was accessed (may equal actorUserId)
     * @param ipAddress     the client IP address
     * @param channel       the channel through which the access occurred (e.g. "WEB")
     */
    @Async
    public void log(Long actorUserId, AuditAction action, String resourceType, Long resourceId,
                    Long patientUserId, String ipAddress, String channel) {
        try {
            auditLogRepository.save(new AuditLog(actorUserId, action, resourceType, resourceId,
                    patientUserId, ipAddress, channel));
        } catch (Exception ex) {
            LOG.error("Failed to write audit log: actor={} action={} resource={}/{}",
                    actorUserId, action, resourceType, resourceId, ex);
        }
    }
}
