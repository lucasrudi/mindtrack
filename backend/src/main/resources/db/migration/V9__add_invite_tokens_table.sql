-- V9__add_invite_tokens_table.sql
CREATE TABLE invite_tokens (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    token          VARCHAR(64) NOT NULL UNIQUE,
    initiator_id   BIGINT NOT NULL,
    initiator_role ENUM('PATIENT','THERAPIST') NOT NULL,
    used_at        TIMESTAMP NULL,
    expires_at     TIMESTAMP NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_it_initiator FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
