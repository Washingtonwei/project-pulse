-- The use case template's missing fields (spec PR #155, docs/design/doc.md "The template's text fields" and
-- "Business rules as links"): a use case names its business rules with CONSTRAINS artifact links from each rule to
-- the use case (BR-use-case-business-rules), and stores Frequency of Use, Associated Information, Assumptions and Open
-- Issues as columns.
--
-- No rows are copied: no environment holds RAM data yet, as for V12.

-- 1. The artifact_link.type value list mirrors ArtifactLinkType.java, kept alphabetical to match Hibernate's
--    generated column definition (the V7 and V12 pattern), which puts CONSTRAINS first. DEFAULT NULL, as V12 left it.

ALTER TABLE `artifact_link`
  MODIFY COLUMN `type` enum(
    'CONSTRAINS','DERIVES_FROM','ENABLES','IMPACTS','MITIGATES','MOTIVATES','PRIMARY_ACTOR','REALIZES','REFERENCES',
    'SECONDARY_ACTOR'
  ) DEFAULT NULL;

-- 2. The four text fields. Frequency of Use is one line of plain text (@Column(length = 255)); the other three are
--    rich text (@Lob String, which Hibernate creates as longtext on MySQL, as for use_case_trigger). All optional.

ALTER TABLE `use_case`
  ADD COLUMN `frequency_of_use` varchar(255) DEFAULT NULL,
  ADD COLUMN `associated_information` longtext,
  ADD COLUMN `assumptions` longtext,
  ADD COLUMN `open_issues` longtext;
