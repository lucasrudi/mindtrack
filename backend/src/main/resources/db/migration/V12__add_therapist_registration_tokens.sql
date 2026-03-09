-- V12__add_therapist_registration_tokens.sql
-- Admin-issued single-use tokens that grant THERAPIST role on redemption.
-- Eliminates per-user admin approval while preventing self-service role promotion (H-3).
CREATE TABLE therapist_registration_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(64) NOT NULL UNIQUE,
    created_by BIGINT NOT NULL,
    used_by    BIGINT NULL,
    used_at    TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trt_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_trt_used_by    FOREIGN KEY (used_by)    REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
