-- Requirement artifacts become first-class (UC-ART-create-artifact, docs/design/art.md, Data model):
--
-- 1. Each artifact carries its own optimistic-lock version (DI-concurrency-control), because it is now created and
--    edited on its own, outside the document section save. NOT NULL DEFAULT 0, so any row that already exists gets a
--    version Hibernate can compare.
-- 2. EXTERNAL_INTERFACE_REQUIREMENT splits into USER_INTERFACE, SOFTWARE_INTERFACE, HARDWARE_INTERFACE and
--    COMMUNICATIONS_INTERFACE, one per SRS interface section, and the OTHER fallback is dropped (BR-artifact-home: each
--    type has one home). Enum values stay alphabetical, to match Hibernate's generated column definition, as in V10.
--
-- No requirement artifact existed in production when this shipped (checked before deploying), so no row is converted.
-- A row still holding a dropped type would make the type statements fail, stopping the deploy rather than losing data.

ALTER TABLE `requirement_artifact`
  ADD COLUMN `version` int NOT NULL DEFAULT 0;

ALTER TABLE `requirement_artifact`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RULE',
    'COMMUNICATIONS_INTERFACE','CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY','EXTERNAL_SYSTEM',
    'FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','HARDWARE_INTERFACE','OPERATING_ENVIRONMENT',
    'POSTCONDITION','PRECONDITION','QUALITY_ATTRIBUTE','RISK','SOFTWARE_INTERFACE',
    'STAKEHOLDER','SUCCESS_METRIC','USER_INTERFACE','USER_STORY','USE_CASE'
  ) DEFAULT NULL;

ALTER TABLE `artifact_key_sequence`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RULE',
    'COMMUNICATIONS_INTERFACE','CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY','EXTERNAL_SYSTEM',
    'FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','HARDWARE_INTERFACE','OPERATING_ENVIRONMENT',
    'POSTCONDITION','PRECONDITION','QUALITY_ATTRIBUTE','RISK','SOFTWARE_INTERFACE',
    'STAKEHOLDER','SUCCESS_METRIC','USER_INTERFACE','USER_STORY','USE_CASE'
  ) NOT NULL;
