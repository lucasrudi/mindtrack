-- V8__add_onboarding_completed.sql
ALTER TABLE user_profiles
  ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;
