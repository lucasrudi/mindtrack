package com.mindtrack.audit.model;

/**
 * Enumeration of auditable actions on PHI resources.
 */
public enum AuditAction {
    READ,
    WRITE,
    DELETE,
    ACCOUNT_DELETION_REQUESTED,
    ACCOUNT_HARD_DELETED
}
