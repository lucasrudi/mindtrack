ALTER TABLE interviews    ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
ALTER TABLE journal_entries ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
ALTER TABLE goals         ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
ALTER TABLE milestones    ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
