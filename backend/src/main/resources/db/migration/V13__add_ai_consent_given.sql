ALTER TABLE user_profiles
    ADD COLUMN ai_consent_given BOOLEAN NOT NULL DEFAULT FALSE;
