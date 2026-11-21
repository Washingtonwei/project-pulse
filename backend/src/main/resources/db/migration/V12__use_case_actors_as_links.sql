-- A use case's actors become artifact links (BR-use-case-actor-types, BR-link-constraints; docs/design/doc.md,
-- "Actors as links"): one PRIMARY_ACTOR link and any number of SECONDARY_ACTOR links from the use case's own
-- artifact to the actor. The use_case.primary_actor_id column and the use_case_secondary_actor table go away.
--
-- No actor rows are copied: no environment holds RAM data yet (staging and production have no use cases, and dev
-- rebuilds its schema and reseeds through UseCaseService, which writes the links itself).

-- 1. The artifact_link.type value list mirrors ArtifactLinkType.java, kept alphabetical to match Hibernate's
--    generated column definition (the V7 pattern). Besides the two actor types it adds ENABLES, which no earlier
--    migration added, so an ENABLES link could not be stored outside dev.

ALTER TABLE `artifact_link`
  MODIFY COLUMN `type` enum(
    'DERIVES_FROM','ENABLES','IMPACTS','MITIGATES','MOTIVATES','PRIMARY_ACTOR','REALIZES','REFERENCES',
    'SECONDARY_ACTOR'
  ) DEFAULT NULL;

-- 2. Drop the old storage: the primary actor's foreign key, its index and its column, then the secondary actor table.

ALTER TABLE `use_case` DROP FOREIGN KEY `FKby3rbcs00t8s24y2yvju5hn1x`;
ALTER TABLE `use_case` DROP INDEX `FKby3rbcs00t8s24y2yvju5hn1x`;
ALTER TABLE `use_case` DROP COLUMN `primary_actor_id`;

DROP TABLE `use_case_secondary_actor`;
