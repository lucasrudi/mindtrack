-- Data retention columns for GDPR Art.17 / CCPA §1798.105 right-to-erasure (issue #166)

ALTER TABLE users
    ADD COLUMN deleted_at     DATETIME NULL,
    ADD COLUMN deletion_scheduled_at DATETIME NULL;

ALTER TABLE user_profiles
    ADD COLUMN anonymized_at DATETIME NULL;

-- Extend audit_logs action enum to support deletion lifecycle events
ALTER TABLE audit_logs
    MODIFY COLUMN action ENUM(
        'READ',
        'WRITE',
        'DELETE',
        'ACCOUNT_DELETION_REQUESTED',
        'ACCOUNT_HARD_DELETED'
    ) NOT NULL;
