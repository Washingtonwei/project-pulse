-- A use case's actors become artifact links (BR-use-case-actor-types, BR-link-constraints; docs/design/doc.md,
-- "Actors as links"): one PRIMARY_ACTOR link and any number of SECONDARY_ACTOR links from the use case's own
-- artifact to the actor. The use_case.primary_actor_id column and the use_case_secondary_actor table go away.
--
-- 1. The artifact_link.type value list mirrors ArtifactLinkType.java, kept alphabetical to match Hibernate's
--    generated column definition (the V7 pattern). Besides the two actor types it adds ENABLES, which no earlier
--    migration added, so an ENABLES link could not be stored outside dev.

ALTER TABLE `artifact_link`
  MODIFY COLUMN `type` enum(
    'DERIVES_FROM','ENABLES','IMPACTS','MITIGATES','MOTIVATES','PRIMARY_ACTOR','REALIZES','REFERENCES',
    'SECONDARY_ACTOR'
  ) DEFAULT NULL;

-- 2. Copy the actors into links. A use case shares its artifact's id (use_case.artifact_id), so that id is the
--    link's source, and the link takes the artifact's team. A null primary actor makes no link. A secondary actor
--    that is also its use case's primary actor is skipped, since one actor may no longer hold both roles. An actor of
--    the wrong type is copied as it is, rather than lost; the editor's next save refuses it and the student picks
--    another. created_by_id, updated_by_id and notes stay null: no user made these links.

INSERT INTO `artifact_link` (`team_team_id`, `source_artifact_id`, `target_artifact_id`, `type`, `created_at`, `updated_at`)
SELECT ra.`team_team_id`, uc.`artifact_id`, uc.`primary_actor_id`, 'PRIMARY_ACTOR', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM `use_case` uc
JOIN `requirement_artifact` ra ON ra.`id` = uc.`artifact_id`
WHERE uc.`primary_actor_id` IS NOT NULL;

INSERT INTO `artifact_link` (`team_team_id`, `source_artifact_id`, `target_artifact_id`, `type`, `created_at`, `updated_at`)
SELECT ra.`team_team_id`, sa.`use_case_id`, sa.`actor_artifact_id`, 'SECONDARY_ACTOR', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM `use_case_secondary_actor` sa
JOIN `use_case` uc ON uc.`artifact_id` = sa.`use_case_id`
JOIN `requirement_artifact` ra ON ra.`id` = uc.`artifact_id`
WHERE uc.`primary_actor_id` IS NULL OR sa.`actor_artifact_id` <> uc.`primary_actor_id`;

-- 3. Drop the old storage: the primary actor's foreign key, its index and its column, then the secondary actor table.

ALTER TABLE `use_case` DROP FOREIGN KEY `FKby3rbcs00t8s24y2yvju5hn1x`;
ALTER TABLE `use_case` DROP INDEX `FKby3rbcs00t8s24y2yvju5hn1x`;
ALTER TABLE `use_case` DROP COLUMN `primary_actor_id`;

DROP TABLE `use_case_secondary_actor`;
