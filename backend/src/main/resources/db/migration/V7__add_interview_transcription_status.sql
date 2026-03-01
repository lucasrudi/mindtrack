-- V7__add_interview_transcription_status.sql
ALTER TABLE interviews
  ADD COLUMN transcription_status
      ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') NULL;
