-- Rename use_case_secondary_actor.stakeholder_artifact_id to actor_artifact_id.
-- A secondary actor is a stakeholder or an external system (BR-use-case-actor-types),
-- so the old name no longer says what the column holds. MySQL 8 keeps the foreign key
-- and the composite primary key across RENAME COLUMN, so nothing else changes.

ALTER TABLE `use_case_secondary_actor` RENAME COLUMN `stakeholder_artifact_id` TO `actor_artifact_id`;
