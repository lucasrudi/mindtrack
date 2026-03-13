CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id   BIGINT NOT NULL,
    action          ENUM('READ','WRITE','DELETE') NOT NULL,
    resource_type   VARCHAR(50) NOT NULL,
    resource_id     BIGINT NOT NULL,
    patient_user_id BIGINT,
    ip_address      VARCHAR(45),
    channel         VARCHAR(20),
    INDEX idx_actor (actor_user_id),
    INDEX idx_patient (patient_user_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
