ALTER TABLE interviews    ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
ALTER TABLE journal_entries ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
ALTER TABLE milestones    ADD COLUMN created_by BIGINT NULL, ADD COLUMN updated_by BIGINT NULL;
-- goals.created_by was already added in V5__add_goal_validation_fields.sql; only updated_by is new
ALTER TABLE goals         ADD COLUMN updated_by BIGINT NULL;
