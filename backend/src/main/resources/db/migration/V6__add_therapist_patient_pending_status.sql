-- V6__add_therapist_patient_pending_status.sql
ALTER TABLE therapist_patients
  MODIFY status ENUM('PENDING','ACTIVE','INACTIVE') NOT NULL DEFAULT 'PENDING';
