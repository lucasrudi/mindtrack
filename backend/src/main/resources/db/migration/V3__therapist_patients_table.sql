-- Therapist-Patient relationship table
CREATE TABLE therapist_patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    therapist_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tp_therapist FOREIGN KEY (therapist_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_tp_patient FOREIGN KEY (patient_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_therapist_patient UNIQUE (therapist_id, patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
