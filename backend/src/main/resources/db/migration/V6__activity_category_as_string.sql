-- Store activity.category by name instead of by ordinal, and add REVIEW and REQUIREMENTS.
-- The column held ActivityCategory ordinals (tinyint, CHECK 0..10), so any change to the
-- enum's order or length would silently remap existing rows. Each ordinal is converted to
-- the name it stood for, in the enum's pre-V6 order.
-- The new value list mirrors ActivityCategory.java (kept alphabetical to match
-- Hibernate's generated column definition).

ALTER TABLE `activity` DROP CHECK `activity_chk_1`;

ALTER TABLE `activity` MODIFY COLUMN `category` varchar(32) DEFAULT NULL;

UPDATE `activity`
SET `category` = ELT(`category` + 1,
  'DEVELOPMENT','TESTING','BUGFIX','COMMUNICATION','DOCUMENTATION','DESIGN',
  'PLANNING','LEARNING','DEPLOYMENT','SUPPORT','MISCELLANEOUS')
WHERE `category` IS NOT NULL;

ALTER TABLE `activity`
  MODIFY COLUMN `category` enum(
    'BUGFIX','COMMUNICATION','DEPLOYMENT','DESIGN','DEVELOPMENT','DOCUMENTATION',
    'LEARNING','MISCELLANEOUS','PLANNING','REQUIREMENTS','REVIEW','SUPPORT','TESTING'
  ) DEFAULT NULL;
