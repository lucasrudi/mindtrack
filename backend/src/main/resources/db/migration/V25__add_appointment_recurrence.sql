ALTER TABLE appointments
    ADD COLUMN duration_minutes INT NOT NULL DEFAULT 50,
    ADD COLUMN recurrence_rule VARCHAR(255) NULL,
    ADD COLUMN series_id VARCHAR(36) NULL,
    ADD COLUMN series_index INT NULL;

CREATE INDEX idx_appointments_series_id ON appointments (series_id);
