-- V27__add_expired_status_to_therapist_patients.sql
ALTER TABLE therapist_patients
  MODIFY status ENUM('PENDING','ACTIVE','INACTIVE','EXPIRED') NOT NULL DEFAULT 'PENDING';
