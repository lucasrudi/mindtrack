ALTER TABLE invite_tokens
    ADD COLUMN recipient_id BIGINT NULL AFTER initiator_id,
    ADD CONSTRAINT fk_it_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE;
