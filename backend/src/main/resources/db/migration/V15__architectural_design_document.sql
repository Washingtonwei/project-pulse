-- The Architectural Design document (OI-21, docs/design/tpl.md): a team's sixth requirement document, in arc42's
-- sections, and the four artifact types its list sections home. Only the two enum columns widen; every value already
-- there is kept. Values stay alphabetical, to match Hibernate's generated column definition, as in V10 and V11 (which
-- last set requirement_artifact.type; V2 set requirement_document.type).
--
-- No row is converted, and no existing SRS loses its architecture sections: production and staging held no requirement
-- documents when this shipped.

ALTER TABLE `requirement_document`
  MODIFY COLUMN `type` enum(
    'ARCHITECTURAL_DESIGN','BUSINESS_RULES','GLOSSARY','SRS','USER_STORIES','USE_CASES','VISION_SCOPE'
  ) DEFAULT NULL;

ALTER TABLE `requirement_artifact`
  MODIFY COLUMN `type` enum(
    'ASSUMPTION','BUSINESS_ASSUMPTION','BUSINESS_DEPENDENCY','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY',
    'BUSINESS_PROBLEM','BUSINESS_RULE','COMMUNICATIONS_INTERFACE','CONSTRAINT','DATA_REQUIREMENT','DEPENDENCY',
    'EXTERNAL_SYSTEM','FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','HARDWARE_INTERFACE','KEY_DECISION',
    'OPERATING_ENVIRONMENT','POSTCONDITION','PRECONDITION','QUALITY_ATTRIBUTE','QUALITY_SCENARIO','RISK',
    'SOFTWARE_INTERFACE','STAKEHOLDER','SUCCESS_METRIC','TECHNICAL_DEBT','TECHNICAL_RISK','USER_INTERFACE',
    'USER_STORY','USE_CASE'
  ) DEFAULT NULL;
