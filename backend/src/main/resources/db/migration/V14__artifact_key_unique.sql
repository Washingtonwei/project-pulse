-- Name-based artifact keys (spec PRs #159 and #164, docs/design/art.md "Artifact keys"): every key is the one a student
-- chose, checked by RequirementArtifactService, so the per-team key counter is retired, and a unique index makes
-- BR-artifact-key-unique hold even on a write path that skips the check. A violation of the index is a teammate
-- taking the key between the check and the write (UC-ART-create-artifact 8c), which the service turns into the same
-- refusal; it recognizes the index by its name, uk_requirement_artifact_team_key (ArtifactKeys.KEY_INDEX), which
-- RequirementArtifact declares too, so the dev and test schema built from the entities has it.
--
-- No key is rewritten: no environment holds requirement artifacts yet, as for V12 and V13. Before the swap, confirm on
-- staging and production that no artifact_key is null and no team holds a key twice:
--   SELECT COUNT(*) FROM requirement_artifact WHERE artifact_key IS NULL;
--   SELECT team_team_id, artifact_key, COUNT(*) FROM requirement_artifact GROUP BY team_team_id, artifact_key HAVING COUNT(*) > 1;

-- 1. The counter is gone: nothing mints keys any more.

DROP TABLE `artifact_key_sequence`;

-- 2. Every artifact has a key, unique within its team. The column stays varchar(255), room for any prefix and a
--    40-character slug. Its utf8mb4_0900_ai_ci collation makes the index ignore case, which changes nothing, since a
--    slug is lowercase.

ALTER TABLE `requirement_artifact`
  MODIFY COLUMN `artifact_key` varchar(255) NOT NULL,
  ADD CONSTRAINT `uk_requirement_artifact_team_key` UNIQUE (`team_team_id`, `artifact_key`);
