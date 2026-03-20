-- Adds therapist-specific color preferences for patient calendar entries.
ALTER TABLE therapist_patients
    ADD COLUMN calendar_color VARCHAR(7) NULL AFTER status;
