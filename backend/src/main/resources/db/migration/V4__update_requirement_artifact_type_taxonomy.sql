-- Update the RequirementArtifactType taxonomy:
--   - remove BUSINESS_RISK (consolidated into RISK)
--   - add DEPENDENCY
-- No data migration is required: prod/staging hold no BUSINESS_RISK or RISK rows,
-- so there are no existing values to remap and no artifact_key_sequence collisions.
-- The new value list mirrors RequirementArtifactType.java (kept alphabetical to match
-- Hibernate's generated column definition).

ALTER TABLE `requirement_artifact`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RULE',
    'CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY','EXTERNAL_INTERFACE_REQUIREMENT','FEATURE',
    'FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','OTHER','POSTCONDITION','PRECONDITION',
    'QUALITY_ATTRIBUTE','RISK','STAKEHOLDER','SUCCESS_METRIC','USER_STORY','USE_CASE',
    'VISION_STATEMENT'
  ) DEFAULT NULL;

ALTER TABLE `artifact_key_sequence`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RULE',
    'CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY','EXTERNAL_INTERFACE_REQUIREMENT','FEATURE',
    'FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','OTHER','POSTCONDITION','PRECONDITION',
    'QUALITY_ATTRIBUTE','RISK','STAKEHOLDER','SUCCESS_METRIC','USER_STORY','USE_CASE',
    'VISION_STATEMENT'
  ) NOT NULL;
