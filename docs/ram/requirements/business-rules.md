# **Requirements Authoring & Management (RAM) Tool**

# **Business Rules**

# **Version \<1.0\>**

# **Revision History**

| Date          | Version | Description | Author   |
| ------------- | ------- | ----------- | -------- |
| \<dd/mmm/yy\> | \<x.x\> | \<details\> | \<name\> |
|               |         |             |          |
|               |         |             |          |
|               |         |             |          |

# **Table of Contents**

- [1. Introduction](#1-introduction)
  - [1.1 Purpose](#11-purpose)
  - [1.2 Scope](#12-scope)
- [2. Business Rules](#2-business-rules)
  - [2.1 Access and Ownership](#21-access-and-ownership)
  - [2.2 Identity and Uniqueness](#22-identity-and-uniqueness)
  - [2.3 Editing and Locking](#23-editing-and-locking)
  - [2.4 Deletion Integrity](#24-deletion-integrity)
  - [2.5 Review and Submission](#25-review-and-submission)
  - [2.6 AI Assistants](#26-ai-assistants)
  - [2.7 Project Source Material](#27-project-source-material)
  - [2.8 Comments and Feedback](#28-comments-and-feedback)

# **Business Rules**

# **1. Introduction**

## **1.1 Purpose**

This document catalogs the business rules for the Requirements Authoring & Management (RAM) Tool — the policies, constraints, computations, and access rules that originate outside the system and apply across multiple use cases and functional requirements. Each rule carries a stable `BR-<n>` identifier. Use cases cite rules by identifier in their business rules field (see [use-cases.md](use-cases.md)) and the Software Requirements Specification references them where a functional or quality requirement enforces one, rather than restating the rule text. Identifiers are append-only and are not renumbered when a rule is inserted; the thematic grouping in Section 2 is organizational only and does not affect a rule's identity.

## **1.2 Scope**

These business rules apply to the RAM module within the Project Pulse platform. They govern who may perform which actions on a team's requirements (access and ownership), how artifacts are identified and kept unique, how concurrent editing is controlled, how deletion preserves integrity, how the review-and-submission workflow constrains editing, how the AI assistants behave, how project source material is handled, and how comments and feedback are governed. Defined terms are catalogued in [project-glossary.md](project-glossary.md).

# **2. Business Rules**

## **2.1 Access and Ownership**

- **BR-1:** A student may view, create, edit, or delete requirement content only within a team she belongs to; she may not access another team's requirements graph, documents, or project source material. (This is the default "Security/access concerns" rule cited by the authoring use cases.)
- **BR-2:** The system enforces role-based access control across the course admin, instructor, and student roles; each operation is permitted only for the roles authorized for it. A course admin is also an instructor of her course and holds every instructor capability in addition to her course-ownership privileges.
- **BR-3:** Only a course admin may create or regenerate a team's requirement documents. Regenerating documents that already contain authored content is destructive and requires an explicit, separate confirmation.
- **BR-4:** Only an instructor assigned to a course section may view or edit that course section's teaching context, per-assistant assistant instructions, and cross-document review criteria, and may enable or disable its AI assistants.

## **2.2 Identity and Uniqueness**

- **BR-5:** Every artifact key is unique within a team and remains stable across edits to the artifact's content.
- **BR-6:** A glossary term name is unique within a team's glossary.
- **BR-7:** A use case name is unique within a team's Use Cases document.
- **BR-8:** A requirement link is unique on the combination of (source artifact, target artifact, link type); an artifact may not link to itself; and a link type is permitted only between artifact types for which it is defined (see the link-type compatibility matrix below).

**Link-type compatibility matrix (BR-8).** Each link is read source → target, and no artifact may link to itself. The matrix classifies *requirement* artifact types into four abstraction tiers, plus a set of *cross-cutting* artifact types:

- Tier 1 — Business/Vision: Business Problem, Business Opportunity, Business Objective, Vision Statement, Success Metric
- Tier 2 — Feature: Feature
- Tier 3 — Behavioral: use case, User Story (and a use case's Preconditions and Postconditions)
- Tier 4 — Detailed/system: Functional Requirement, Quality Attribute, External Interface Requirement, Constraint, Data Requirement
- Cross-cutting: Stakeholder, Risk, Assumption, Dependency, Business Rule, glossary term

A lower tier number is a higher level of abstraction (Tier 1 is the highest). The permitted source → target combinations per link type are:

| Link type | Permitted source → target | Direction rule |
| ---- | ---- | ---- |
| DERIVES_FROM | A Tier 1–4 requirement artifact → a Tier 1–4 requirement artifact | Source tier number ≥ target tier number (a link must not point from a higher-level artifact down to a lower-level one); same-tier decomposition is allowed. Use DERIVES_FROM when the source was decomposed/derived from the target. |
| REALIZES | A Tier 2–4 requirement artifact → a Tier 1–4 requirement artifact | Same tier-direction constraint as DERIVES_FROM. Use REALIZES when the source fulfills or satisfies the target rather than being decomposed from it (e.g., use case → Feature, Feature → Business Objective). A future release adds design/implementation artifacts as sources → Tier 4 targets. |
| REFERENCES | Any artifact → any artifact | The general-purpose catch-all; no tier constraint. Use it (and only it) when an endpoint is a glossary term, or when no more specific link type applies. |
| IMPACTS | Quality Attribute, Constraint, Business Rule, Risk, or Dependency → any requirement artifact | Source is a cross-cutting or constraining artifact whose change or presence affects the target. |
| MITIGATES | Functional Requirement, Quality Attribute, Constraint, Business Rule, use case, or Feature → a Risk | Target must be a Risk (any category — business, technical, or security/safety). |
| MOTIVATES | Stakeholder, Business Problem, or Business Opportunity → any requirement artifact | Source expresses a need or driver that motivates the target. |

The artifact types named here are the canonical `RequirementArtifactType` set reconciled in OI-15 (glossary ↔ SRS §7.1 enum); Stakeholder and Dependency are tracked artifact types. User stories are a deferred type (see SRS §7.1); a User Story, when present, is a Tier 3 behavioral artifact.

## **2.3 Editing and Locking**

- **BR-9:** Editing a document section or a use case requires an exclusive lock on that authoring destination, held by the editing student; at most one student may hold a given lock at a time.
- **BR-10:** An edit lock held without activity longer than a configurable timeout (default 60 seconds) is released automatically.
- **BR-11:** Real-time collaboration shall not overwrite or corrupt content already saved by another collaborator.

## **2.4 Deletion Integrity**

- **BR-12:** A glossary term or requirement artifact that is still referenced by another artifact may not be deleted; the references must be removed or repointed first. Deletion is logical (soft delete): deleted items leave active use but are retained for audit and are not reusable.

## **2.5 Review and Submission**

- **BR-13:** On submission for review, a requirement document is locked for review and becomes read-only to all students on the team until an instructor returns it for revision.
- **BR-14:** Only an instructor assigned to the course section may review, accept, or return that team's submitted documents; returning a document for revision unlocks it for student editing.

## **2.6 AI Assistants**

- **BR-15:** An AI assistant is available to a course section's students only while the instructor has enabled it for that course section. The drafting assistant is disabled by default.
- **BR-16:** Assistants are Socratic: they ask, critique, explain, structure, and route, but never author or silently insert finished requirement content on a student's behalf.
- **BR-17:** Assistant-proposed content is applied only through explicit, per-item acceptance by the student; the system provides no "accept all" shortcut, and every finding or proposal carries an instructive rationale.
- **BR-18:** Where a team's authored-and-verified requirements conflict with its imported project source material, the authored content prevails; an assistant shall not treat the (possibly stale) pitch materials as authoritative over current verified requirements.

## **2.7 Project Source Material**

- **BR-19:** Only a member of the team that owns the project may import its project source material; imported materials are project inputs, not authored requirement content, and are not graded as requirements.

## **2.8 Comments and Feedback**

- **BR-20:** A comment may be created and resolved by any student on the team that owns the commented requirement document, document section, or requirement artifact, and by the instructor assigned to the course section. Because comments record discussion and feedback rather than authored requirement content, commenting and resolving are permitted regardless of whether the document is locked for review; the review lock (BR-13) restricts edits to requirement content only, not commenting.
