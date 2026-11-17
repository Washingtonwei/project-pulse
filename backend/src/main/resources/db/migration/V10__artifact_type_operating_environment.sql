-- RequirementArtifactType gains OPERATING_ENVIRONMENT (the SRS Operating Environment section is now a list) and loses
-- VISION_STATEMENT (a vision statement is narrative, not an artifact), so both type columns change to the new list
-- (UC-ART-create-artifact, docs/design/art.md, Data model). Enum values stay alphabetical, to match Hibernate's
-- generated column definition, as in V4 and V7.
--
-- No requirement document or artifact existed in production when this shipped (checked before deploying), so no row
-- holds VISION_STATEMENT and no document is reshaped; new documents take their sections from the templates. A row
-- that did hold VISION_STATEMENT would make these statements fail, stopping the deploy rather than losing data.

ALTER TABLE `requirement_artifact`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RULE',
    'CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY','EXTERNAL_INTERFACE_REQUIREMENT','EXTERNAL_SYSTEM',
    'FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','OPERATING_ENVIRONMENT','OTHER','POSTCONDITION',
    'PRECONDITION','QUALITY_ATTRIBUTE','RISK','STAKEHOLDER','SUCCESS_METRIC','USER_STORY','USE_CASE'
  ) DEFAULT NULL;

ALTER TABLE `artifact_key_sequence`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RULE',
    'CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY','EXTERNAL_INTERFACE_REQUIREMENT','EXTERNAL_SYSTEM',
    'FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','OPERATING_ENVIRONMENT','OTHER','POSTCONDITION',
    'PRECONDITION','QUALITY_ATTRIBUTE','RISK','STAKEHOLDER','SUCCESS_METRIC','USER_STORY','USE_CASE'
  ) NOT NULL;
