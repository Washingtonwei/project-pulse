-- Order each use case's preconditions and postconditions (BR-use-case-condition-shared).
-- UseCase.preconditions and .postconditions became lists with @OrderColumn, so each join table gets
-- an index column, and its primary key becomes the one Hibernate generates for an indexed list:
-- (index, use_case_id). The old key (artifact_id, use_case_id) would collide while a reorder rewrites
-- the artifact at each index. Existing rows are numbered from 0 (@OrderColumn is 0-based) by artifact
-- id, since their old order was undefined. The artifact column gets its own index first, because the
-- old primary key was the index its foreign key used. Column, key and index names match Hibernate's
-- generated schema, which the dev profile and the integration tests run against.

-- use_case_precondition

ALTER TABLE `use_case_precondition` ADD COLUMN `precondition_index` int DEFAULT NULL FIRST;

UPDATE `use_case_precondition` p
JOIN (
  SELECT `use_case_id`, `precondition_artifact_id`,
         ROW_NUMBER() OVER (PARTITION BY `use_case_id` ORDER BY `precondition_artifact_id`) - 1 AS `idx`
  FROM `use_case_precondition`
) numbered
  ON numbered.`use_case_id` = p.`use_case_id`
 AND numbered.`precondition_artifact_id` = p.`precondition_artifact_id`
SET p.`precondition_index` = numbered.`idx`;

ALTER TABLE `use_case_precondition` MODIFY COLUMN `precondition_index` int NOT NULL;

ALTER TABLE `use_case_precondition` ADD KEY `FKmw70ecnof4ry4unj25evv9hmd` (`precondition_artifact_id`);

ALTER TABLE `use_case_precondition` DROP PRIMARY KEY, ADD PRIMARY KEY (`precondition_index`, `use_case_id`);

-- use_case_postcondition

ALTER TABLE `use_case_postcondition` ADD COLUMN `postcondition_index` int DEFAULT NULL FIRST;

UPDATE `use_case_postcondition` p
JOIN (
  SELECT `use_case_id`, `postcondition_artifact_id`,
         ROW_NUMBER() OVER (PARTITION BY `use_case_id` ORDER BY `postcondition_artifact_id`) - 1 AS `idx`
  FROM `use_case_postcondition`
) numbered
  ON numbered.`use_case_id` = p.`use_case_id`
 AND numbered.`postcondition_artifact_id` = p.`postcondition_artifact_id`
SET p.`postcondition_index` = numbered.`idx`;

ALTER TABLE `use_case_postcondition` MODIFY COLUMN `postcondition_index` int NOT NULL;

ALTER TABLE `use_case_postcondition` ADD KEY `FKavn1ufxtfgrwvnlwos92hniyo` (`postcondition_artifact_id`);

ALTER TABLE `use_case_postcondition` DROP PRIMARY KEY, ADD PRIMARY KEY (`postcondition_index`, `use_case_id`);
