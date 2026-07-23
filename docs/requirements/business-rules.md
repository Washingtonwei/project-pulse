# **Project Pulse**

# **Business Rules**

# **Version 1.0**

# **Revision History**

| Date          | Version | Description | Author   |
| ------------- | ------- | ----------- | -------- |
| \<dd/mmm/yy\> | \<x.x\> | \<details\> | \<name\> |
|               |         |             |          |
|               |         |             |          |
|               |         |             |          |

# **Introduction**

## **Purpose**

This document catalogs the business rules for Project Pulse — the policies, constraints, computations, and access rules that originate outside the system and apply across multiple use cases and functional requirements. Each rule carries a stable `BR-<slug>` identifier — a name-based slug coined from the rule's gist. Use cases cite rules by identifier in their business rules field (see [use-cases.md](use-cases.md)) and the Software Requirements Specification references them where a functional or quality requirement enforces one, rather than restating the rule text. Identifiers are stable handles, never renumbered; the thematic grouping into sections is organizational only and does not affect a rule's identity.

## **Scope**

These business rules apply across Project Pulse. The course-administration rules govern who may create and configure course sections, invite and manage student and instructor accounts, form teams and assign their members, and manage rubrics. The performance-tracking rules govern how teams are staffed with their single instructor and how the weekly activity report and peer evaluation workflows are constrained (active weeks, submission windows, edit and visibility policy). The requirements-authoring rules govern who may perform which actions on a team's requirements (access and ownership), how artifacts are identified and kept unique, how concurrent editing is controlled, how deletion preserves integrity, how the review-and-submission workflow constrains editing, how the AI assistants behave, how project source material is handled, and how comments and feedback are governed. Defined terms are catalogued in [project-glossary.md](project-glossary.md).

# **Course Administration**

- **BR-section-admin-only:** Only a course admin may create or edit a course section, configure its active-weeks window (per BR-active-weeks), or assign a rubric to it.
- **BR-invitations-admin-only:** Only a course admin may invite students to a course section or invite instructors to register an account. Each invitation is delivered by email with a registration link unique to the invitee.
- **BR-account-self-setup:** A student or instructor sets up her own account by using the unique registration link from her invitation email; the system permits self-setup only against an unconsumed invitation.
- **BR-team-admin-only:** Only a course admin may create, edit, or delete a team within a course section, and only a course admin may assign students or instructors to a team or remove them from one. The single-instructor cardinality per team is governed by BR-team-single-instructor.
- **BR-student-lifecycle:** A course admin, or an instructor assigned to the student's course section, may deactivate or reactivate a student — deactivation revokes the student's access while preserving her submitted work. Only a course admin may delete a student; deletion is physical and unrecoverable and also removes the student's weekly activity reports and peer evaluations.
- **BR-instructor-lifecycle:** Only a course admin may deactivate or reactivate an instructor; deactivation revokes the instructor's access while retaining her record.
- **BR-rubric-admin-only:** Only a course admin may create, edit, or delete a rubric or its criteria.

# **Teams and Instructor Assignment**

- **BR-team-single-instructor:** Each team is assigned a single instructor — its TCU instructor; an instructor may be assigned to multiple teams.

# **Weekly Activity Report and Peer Evaluation**

- **BR-active-weeks:** A student may submit a peer evaluation only for a week that is one of her course section's active weeks; combined with the previous-week rule (BR-evaluation-submission-window), each active week is evaluated during the *following* calendar week — so the first active week is evaluated in the second active week, and the last active week is evaluated in the week after the active window closes (which need not itself be active). The gate is on the evaluated week being active, not on the date of submission. A weekly activity report, by contrast, may be submitted regardless of the active-weeks window. A course section's active weeks are configured per section by its course admin (BR-section-admin-only, UC-SEC-setup-active-weeks).
    - *Active-weeks guidance (instructor reference, not enforced by the system): fall sections usually run weeks 5 through 15 of the semester (winter holidays inactive) and spring sections weeks 1 through 15. The enforced window is whatever the course admin configures per section.*
- **BR-evaluation-editable-until-close:** A peer evaluation remains editable by its evaluator while its submission window is open — re-submitting updates the existing evaluation in place. The system applies no separate finalize or completion action; the close of the submission window (BR-evaluation-submission-window) is itself the lock that makes the evaluation read-only.
- **BR-evaluation-submission-window:** A student may submit a peer evaluation only for the previous week, and has that one week to complete it; both the initial submission and any later edits must occur within this window. A student who fails to complete a peer evaluation in that window cannot make it up, and an evaluation can no longer be changed once its window has closed.
- **BR-evaluation-private-comment:** When submitting a peer evaluation, the evaluator may optionally include a private comment about the teammate being evaluated. A private comment is visible only to the instructor assigned to the course section (and course admin, per BR-role-based-access); it is never shown to the evaluatee or any other student on the team. Private comments exist to give students a safe channel to raise concerns early.
- **BR-evaluation-visibility:** For a peer evaluation, a student may see only her own rubric criterion scores, public comments (not private comments — see BR-evaluation-private-comment), and overall grade.

# **Access and Ownership**

- **BR-team-scoped-access:** A student may view, create, edit, or delete requirement content only within a team she belongs to; she may not access another team's requirements graph, documents, or project source material. (This is the default "Security/access concerns" rule cited by the authoring use cases.)
- **BR-role-based-access:** The system enforces role-based access control across the course admin, instructor, and student roles; each operation is permitted only for the roles authorized for it. A course admin is also an instructor of her course and holds every instructor capability in addition to her course-ownership privileges.
- **BR-document-creation:** Only a course admin may create a team's requirement documents, and each document type is created at most once per team — an existing document cannot be regenerated or overwritten.
- **BR-section-config-access:** Only an instructor assigned to a course section may view or edit that course section's teaching context, per-assistant assistant instructions, and cross-document review criteria, and may enable or disable its AI assistants.

# **Identity and Uniqueness**

- **BR-artifact-key-unique:** Every artifact key is unique within a team and remains stable across edits to the artifact's content.
- **BR-glossary-term-unique:** A glossary term name is unique within a team's glossary, so each term carries a single authoritative definition.
- **BR-use-case-name-unique:** A use case name is unique within a team's Use Cases document.
- **BR-link-constraints:** A requirement link is unique on the combination of (source artifact, target artifact, link type); an artifact may not link to itself; and a link type is permitted only between artifact types for which it is defined (see the link-type compatibility matrix below).

**Link-type compatibility matrix (BR-link-constraints).** Each link is read source → target, and no artifact may link to itself. The matrix classifies *requirement* artifact types into four abstraction tiers, plus a set of *cross-cutting* artifact types:

- Tier 1 — Business/Vision: Business Problem, Business Opportunity, Business Objective, Vision Statement, Success Metric
- Tier 2 — Feature: Feature
- Tier 3 — Behavioral: Use Case, User Story
- Tier 4 — Detailed/system: Functional Requirement, Quality Attribute, External Interface Requirement, Constraint, Data Requirement, Operating Environment
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

The artifact types named here are the canonical `RequirementArtifactType` set (glossary ↔ the SRS's Business Domain Model enum) — the **product's student-facing taxonomy** for typing artifacts in a team's own requirements graph, **not** this spec's own requirement-class/ID inventory (the two share names but are different lists; see the `RequirementArtifactType` note in the SRS's Business Domain Model). Stakeholder and Dependency are tracked artifact types. User stories are a deferred type (see the SRS's Business Domain Model); a User Story, when present, is a Tier 3 behavioral artifact.

# **Editing and Locking**

- **BR-edit-lock-required:** Editing a document section or a use case requires an exclusive lock on that authoring destination, held by the editing student; at most one student may hold a given lock at a time.
- **BR-lock-expiry:** An edit lock expires a configurable interval after it is acquired (default 15 minutes) and is then released automatically, freeing the authoring destination for another student; an expired lock is cleared the next time the destination is read or a lock on it is requested.
- **BR-collab-no-overwrite:** Real-time collaboration shall not overwrite or corrupt content already saved by another collaborator.

# **Deletion Integrity**

- **BR-deletion-integrity:** A glossary term or requirement artifact that is still referenced by another artifact may not be deleted; the references must be removed or repointed first. Deletion is logical (soft delete): deleted items leave active use but are retained for audit and are not reusable.

# **Review and Submission**

- **BR-review-lock:** On submission for review, a requirement document is locked for review and becomes read-only to all students on the team until an instructor returns it for revision.
- **BR-review-authority:** Only an instructor assigned to the course section may review, accept, or return that team's submitted documents; returning a document for revision unlocks it for student editing.

# **AI Assistants**

- **BR-assistant-enablement:** An AI assistant is available to a course section's students only while the instructor has enabled it for that course section. The drafting assistant is disabled by default.
- **BR-assistant-socratic:** Assistants are Socratic: they ask, critique, explain, structure, and route, but never author or silently insert finished requirement content on a student's behalf.
- **BR-explicit-acceptance:** Assistant-proposed content is applied only through explicit, per-item acceptance by the student; the system provides no "accept all" shortcut, and every finding or proposal carries an instructive rationale.
- **BR-authored-prevails:** Where a team's authored-and-verified requirements conflict with its imported project source material, the authored content prevails; an assistant shall not treat the (possibly stale) pitch materials as authoritative over current verified requirements.

# **Project Source Material**

- **BR-source-material-import:** Only a member of the team that owns the project may import its project source material; imported materials are project inputs, not authored requirement content, and are not graded as requirements.

# **Comments and Feedback**

- **BR-comment-access:** A comment may be created and resolved by any student on the team that owns the commented requirement document, document section, or requirement artifact, and by the instructor assigned to the course section. Because comments record discussion and feedback rather than authored requirement content, commenting and resolving are permitted regardless of whether the document is locked for review; the review lock (BR-review-lock) restricts edits to requirement content only, not commenting.
