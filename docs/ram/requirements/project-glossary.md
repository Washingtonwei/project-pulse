# **Requirements Authoring & Management (RAM) Tool**

# **Glossary**

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
  - [1.3 References](#13-references)
  - [1.4 Overview](#14-overview)
- [2. Definitions](#2-definitions)
  - [Requirement Artifact](#requirement-artifact)
  - [Artifact Type](#artifact-type)
  - [Artifact Key](#artifact-key)
  - [Requirement Link](#requirement-link)
  - [Artifact Link Type](#artifact-link-type)
  - [Graph-First Model](#graph-first-model)
  - [Requirements Graph](#requirements-graph)
  - [Traceability](#traceability)
  - [Graph Navigation](#graph-navigation)
  - [Requirement Document](#requirement-document)
  - [Document Section](#document-section)
  - [Section Key](#section-key)
  - [Template](#template)
  - [Authoring Destination](#authoring-destination)
  - [Business Objective](#business-objective)
  - [Risk](#risk)
  - [Business Rule](#business-rule)
  - [Feature](#feature)
  - [Use Case](#use-case)
  - [Functional Requirement](#functional-requirement)
  - [Constraint](#constraint)
  - [Quality Attribute](#quality-attribute)
  - [External Interface Requirement](#external-interface-requirement)
  - [Glossary Term](#glossary-term)
  - [Stakeholder](#stakeholder)
  - [Assumption](#assumption)
  - [Dependency](#dependency)
  - [Validation (ReqLint)](#validation-reqlint)
  - [Keyword](#keyword)
  - [Assistant](#assistant)
  - [Assistant Instructions](#assistant-instructions)
  - [Teaching Context](#teaching-context)
  - [Cross-Document Review Criteria](#cross-document-review-criteria)
  - [Project Source Material](#project-source-material)
  - [Team](#team)
  - [Course](#course)
  - [Course Section](#course-section)
  - [Course Admin](#course-admin)
  - [Instructor](#instructor)
  - [Student](#student)
  - [MVP](#mvp)
  - [FERPA](#ferpa)
  - [EARS](#ears)
  - [INVEST](#invest)
  - [Wiegers Requirement Categories](#wiegers-requirement-categories)
  - [SSO](#sso)
  - [LLM](#llm)
  - [WCAG](#wcag)

# **1. Introduction**

## **1.1 Purpose**

This document is used to define terms used throughout the planning, design, and software development of this project. To be used by current and future software engineers as well as stakeholders to reference key terms that may cause confusion or any terms that need clarification. The glossary allows all parties involved in the project to have a standard definition of vocabulary, terms, or phrases. Also, this glossary will help define complex concepts and various abbreviations used throughout the project.

## **1.2 Scope**

This glossary is associated with the Requirements Authoring & Management (RAM) Tool project. The terms in this document will be used in the Vision and Scope document, the Use Cases document, the Business Rules document, the Software Requirements Specification, and other project documents.

## **1.3 References**

## **1.4 Overview**

The remainder of this document contains terms followed by their definitions. Terms are organized thematically and identified by name rather than number, so related terms sit together and new terms can be inserted into their group without renumbering.

# **2. Definitions**

## **Requirement Artifact**

The core domain entity representing a traceable requirements engineering concept. Artifacts are modeled explicitly to enable traceability, validation, navigation, and analysis. RAM models requirements as atomic artifacts (e.g., objective, feature, use case, step, functional requirement, glossary term), each with identity, type, and explicit links.

A requirement artifact may represent:

- a requirement:
  - a business problem
  - a business opportunity
  - a business objective
  - a success metric
  - a vision statement
  - a feature
  - a use case
  - a use-case precondition
  - a use-case postcondition
  - a user story *(recognized but not authored in the MVP — reserved for a future, optional User Stories document)*
  - a business rule
  - a functional requirement
  - a quality attribute
  - an external interface requirement
  - a constraint
  - a data requirement
- a non-requirement concept:
  - a glossary term
  - a stakeholder
  - a risk
  - an assumption
  - a dependency

These map one-to-one to the `RequirementArtifactType` enumeration in the data model (SRS §7.1), which is the authoritative taxonomy; the `OTHER` enum value is an implementation fallback and is not a distinct domain concept. "Background" is document narrative (a prose document section), not a tracked artifact.

Requirement artifacts are:

- graph nodes
- team-scoped
- independent of document structure
- connected via typed traceability links

| Artifact Type      | title means    | content means         |
| :----------------- | :------------- | :-------------------- |
| Glossary term      | term           | definition            |
| FR                 | Short summary  | The system shall…     |
| Business Objective | Objective name | Objective description |
| Feature            | Feature name   | Feature description   |
| Use Case           | Use case name  | Brief description     |

## **Artifact Type**

A predefined classification that specifies the semantic role of a requirement artifact within the requirements model.

Notes / Clarifications:

- Artifact types are selected from a system-defined list.
- The artifact type determines how an artifact participates in validation, traceability, and visualization.
- Artifact types help enforce structure and consistency across requirement documents.

Examples:

- BUSINESS_OBJECTIVE — A high-level goal the system must achieve.
- FEATURE — A capability that delivers value to users.
- USE_CASE — A structured description of system behavior from an actor's perspective.
- FUNCTIONAL_REQUIREMENT — A specific, testable system behavior.
- GLOSSARY_TERM — A defined term used consistently across documents.

## **Artifact Key**

A unique, system-assigned identifier used to unambiguously reference a requirement artifact within a project. An artifact key remains stable over time, even if the artifact's content is edited.

Notes / Clarifications:

- Artifact keys are generated by the system and are not editable by users.
- Each artifact key is unique within a team.
- Artifact keys are used in traceability links, search, exports, and grading.
- Artifact keys do not change when the wording of a requirement changes.

Examples:

- BO-3 — business objective #3
- FR-12 — functional requirement #12
- UC-5 — use case #5
- GL-7 — glossary term #7

In the MVP, artifact keys are assigned as a simple running sequence per artifact type (`FR-1`, `FR-2`, …; `UC-1`, `UC-2`, …), with the system tracking the current index per type and incrementing on each new artifact. A categorical scheme (e.g., the SRS's `FR-SAVE*` / `FR-LOCK*` families) may be adopted in a later version. These product-generated artifact keys are distinct from the area-prefixed `UC-<AREA>-<n>` identifiers (e.g., `UC-GLO-1`) used to organize the Use Cases document of this specification itself.

## **Requirement Link**

A directed, typed relationship between two requirement artifacts.

Each requirement link has:

- exactly one source artifact
- exactly one target artifact
- a link type describing the semantic relationship

The link type is one of the values defined by artifact link type — covering derivation, realization, reference, impact, mitigation, and motivation relationships.

## **Artifact Link Type**

An enumeration defining the allowed semantic relationships between requirement artifacts. Each value is read source → target:

- DERIVES_FROM — the source (lower-level) artifact is derived from a higher-level target (e.g., functional requirement → use case → feature → objective).
- REALIZES — the source artifact realizes or implements a more abstract target (e.g., implementation → design → functional requirement / use case / feature / objective).
- REFERENCES — the source artifact makes a general reference to the target, with no more specific relationship.
- IMPACTS — the source (a change, quality attribute, or risk) affects the target requirement or artifact; a cross-cutting link used for impact analysis.
- MITIGATES — the source (a control or requirement) mitigates the target risk or threat.
- MOTIVATES — the source stakeholder motivates the target requirement.

## **Graph-First Model**

An architectural approach in which requirements engineering concepts are modeled primarily as a graph of artifacts and links, rather than as text embedded in documents.

In RAM:

- artifacts are graph nodes
- links are graph edges
- documents are views over the graph

## **Requirements Graph**

The complete set of requirement artifacts and requirement links for a project, viewed as a directed graph. The requirements graph is the underlying data model from which all requirement documents are rendered. It is team-scoped and supports navigation, traceability queries, impact analysis, and validation.

## **Traceability**

The ability to follow relationships between requirement artifacts across different abstraction levels.

Examples:

- business objective → feature → use case → functional requirement
- glossary term → all referencing artifacts
- Traceability is a core learning and analysis objective of RAM.

## **Graph Navigation**

The ability to traverse requirement artifacts and requirement links interactively to understand relationships, coverage, and impact.

This capability differentiates RAM from traditional document-based tools.

## **Requirement Document**

A structured view container used to organize and present requirement artifacts for authoring, review, export, and grading.

Examples include:

- Vision and Scope
- Glossary
- Business Rules
- Use Cases
- Software Requirements Specification (SRS)

## **Document Section**

A template-defined document section within a requirement document.

A document section specifies:

- document section title
- ordering
- whether the document section is required
- guidance or examples
- whether the document section contains narrative text or a list of artifacts

Document sections do not own artifacts; they define where artifacts are rendered.

## **Section Key**

A stable, semantic identifier that links requirement artifacts to document sections for rendering purposes.

Examples:

- vision.businessObjectives
- srs.functionalRequirements
- glossary.terms

Section keys remain stable even if document section titles or ordering change.

## **Template**

A predefined blueprint that specifies which requirement documents a team receives and the document sections each one contains. Instantiating a template creates the concrete, empty documents and document sections in which a team authors its requirements.

In the MVP, templates are built-in and fixed (hardcoded); template customization is out of scope. A course admin instantiates a team's documents from its built-in templates.

## **Authoring Destination**

The document section or use case that an authored edit — or an accepted assistant candidate — is applied to, and that the author locks while editing. An authoring destination is either a document section of a section-based requirement document (narrative text or an artifact list) or a use case in the Use Cases document, which is not section-based and whose use cases are artifacts in their own right.

An authoring destination is the unit of locking and the placement target for accepted AI candidates: an authoring or elicitation action is always scoped to a single authoring destination, and content is applied to it only while the author holds its lock.

## **Business Objective**

A requirement artifact describing a measurable goal or outcome that the system is intended to achieve from a business or organizational perspective.

## **Risk**

A requirement artifact describing a potential event or condition that could negatively impact the success of the system or project. A risk may be a business/adoption risk (value, uptake, compliance), a technical/feasibility risk (unproven technology, integration, scalability), or a security/safety risk or threat; the category may be recorded as optional metadata. A control or requirement that reduces a risk links to it via the MITIGATES artifact link type.

A risk may be mitigated by other artifacts (it is the target of a MITIGATES link) and may impact requirements (the source of an IMPACTS link).

## **Business Rule**

A statement of policy, regulation, computation, or organizational practice that constrains or directs how the system must behave. Business rules originate outside the system and apply across multiple requirements.

Examples:

- A requirement artifact that is still referenced by another artifact may not be deleted.
- All requirement artifact keys assigned within a team must be unique.

## **Feature**

A high-level system capability that groups related functional behavior.

Features often act as an intermediate abstraction between business objectives and use cases.

## **Use Case**

A behavioral description of how an external actor interacts with the system to achieve a specific goal.

## **Functional Requirement**

A requirement artifact describing a specific, testable behavior the system must perform. Functional requirements are typically expressed as "shall" statements and are identified by an artifact key that RAM assigns (in the MVP, a per-type running sequence: `FR-1`, `FR-2`, …).

Examples: FR-1 (the system autosaves the active document section every 10 seconds), FR-2 (the system grants a student an exclusive lock before editing a document section), FR-3 (the system assigns a unique key to each new artifact).

Note: the area-prefixed form `FR-<AREA>-<n>` (e.g., `FR-SAVE-1`, `FR-LOCK-1`) is not a product-generated artifact key — it is the organizing convention used by this specification's own SRS §5.2, parallel to the `UC-<AREA>-<n>` use-case IDs. See artifact key.

## **Constraint**

A requirement artifact describing a condition or restriction imposed on the system's design, implementation, or operation that limits the solution space. Constraints commonly originate from regulations, organizational policy, mandated technologies, or hardware limits.

## **Quality Attribute**

A requirement artifact describing a measurable non-functional characteristic of the system — such as performance, security, usability, availability, or robustness — and the criteria by which it is judged.

## **External Interface Requirement**

A requirement artifact describing how the system interacts with an external entity: users (user interfaces), other software systems (software interfaces and APIs), hardware, or communication channels.

## **Glossary Term**

A requirement artifact defining a domain-specific term used consistently across all requirement documents.

Glossary terms support:

- terminology consistency
- reference tracking
- safe rename and "find usages" behavior

## **Stakeholder**

A requirement artifact describing any person, group, or organization with an interest in or influence over the system. Includes end users, but also customers, sponsors, instructors, administrators, regulators, and others affected by the system.

## **Assumption**

A requirement artifact stating a condition taken to be true for purposes of planning or specification but not yet verified. Assumptions that turn out to be false may invalidate requirements that depend on them.

## **Dependency**

A requirement artifact identifying an external factor outside the project's control that the system relies on (e.g., third-party APIs, infrastructure availability, decisions by other teams). Project success may be affected if a dependency fails or changes.

## **Validation (ReqLint)**

Deterministic, rule-based checks that enforce:

- completeness
- structure
- writing standards
- naming conventions
- terminology consistency

Validation operates on requirement artifacts and document structure, independent of AI assistance.

## **Keyword**

A word or phrase entered by a user to search for requirement artifacts or documents that contain related content.

Notes / Clarifications:

- Keywords are user-provided and not required to be unique.
- Keywords are matched against artifact content, document content, titles, and identifiers.
- Keywords are used only for searching and navigation and are not stored as identifiers.

Examples:

- Searching for login returns artifacts and documents related to authentication.
- Searching for FR-12 returns the functional requirement with that artifact key.
- Searching for order processing returns artifacts mentioning that phrase.

## **Assistant**

An AI participant configured for a specific pedagogical role in requirements authoring — for example the elicitation assistant, critique assistant, tutor assistant, client role-play assistant, structuring assistant, drafting assistant, or the project-level project assistant that orients students and routes them to the other assistants. Assistants are deliberately Socratic: they bias toward asking probing questions, surfacing what is missing, and challenging vague language so that the student produces the next draft, rather than handing the student finished requirements. Each assistant behaves according to its assistant instructions and draws on the teaching context configured for its course section, and is realized through the external LLM service via the server-side AI proxy. An instructor enables or disables assistants per course section, and assistant-proposed content is applied only through an explicit propose → inspect → accept loop (no "accept all").

## **Assistant Instructions**

Instructor-authored, per-assistant directives that govern how an assistant behaves: its role (what the assistant does), its persona and tone (for example, Socratic, patient, warm, like an experienced requirements engineer), and what it must avoid (for example, handing students finished requirements rather than eliciting their next draft). Assistant instructions reference the course section's teaching context for the standards the assistant enforces, so behavior is configured per assistant while standards are shared across the course section's assistants. They are a first-class, instructor-controlled teaching artifact, analogous in purpose to a project's authoring guide for an AI assistant.

## **Teaching Context**

Instructor-authored, course section-level standards an assistant is held to when critiquing student work — the curriculum encoded as machine-usable guidance. It captures the requirement-quality criteria students are graded against (e.g., INVEST, testability, Wiegers requirement categories), common student mistakes to watch for, and the thinking order the course wants students to follow. The teaching context is shared by all of a course section's assistants and referenced by their assistant instructions; it is a first-class, instructor-controlled teaching artifact maintained per course section.

## **Cross-Document Review Criteria**

Instructor-authored (or course admin-authored), course section-level criteria the critique assistant applies when reviewing a team's entire set of requirement documents together — a *whole-project review* (UC-AI-10) — for cross-document gaps, inconsistencies, conflicts, and broken traceability. They specify the dimensions and standards the whole-project review checks across documents (for example: every feature has a downstream use case and functional requirement; every cited term, business rule, and FR resolves; scope statements agree across documents).

Distinct from the two other AI teaching artifacts: the teaching context supplies shared, per-requirement quality standards every assistant is held to, and assistant instructions supply per-assistant behavioral directives; the cross-document review criteria instead define *what the whole-project review evaluates across the document set*. They must be defined for a course section before a student can request a whole-project review (UC-AI-10), and are a first-class, instructor-controlled teaching artifact maintained per course section.

## **Project Source Material**

Input materials the client provides to seed a project — typically a pitch slide deck and a short brief (PDF) covering background, stakeholders, problem statement, users, objectives, desired functionality, possible solutions, prototypes, and a candidate tech stack. The team imports these into RAM, and the assistants use them as context (for example, the elicitation assistant's gap analysis and interview-question preparation). Project source material is an input the team works from, not authored requirement content; it may become stale after the project starts; it is distinct from requirement documents and requirement artifacts.

## **Team**

A group of users (students or instructors) collaborating on a single project.

Teams are the ownership boundary for requirement artifacts and documents.

## **Course**

An academic course hosted on the Project Pulse platform (e.g., COSC 40943 Senior Design I). The user who creates a Course becomes its course admin; a Course contains one or more course sections offered across terms.

## **Course Section**

A specific offering of a Course within an academic term (e.g., Fall 2025, Section 01). A course section enrolls students and instructors and groups students into teams. Distinct from document section, which is a structural division within a requirement document.

## **Course Admin**

A course-scoped owner role in Project Pulse. The user who creates a Course becomes its course admin; different Courses have different course admins. A course admin is also an instructor of her Course and therefore holds every instructor capability; in addition, she invites instructors to the Course, creates course sections and teams, manages instructor and student enrollment, assigns teams, and configures which built-in templates are available to each team. Provisioning a team's requirement documents from those templates is reserved to the course admin (UC-TPL-1, BR-3).

## **Instructor**

A user role that teaches a course section, invited to the Course by its course admin (who is herself also an instructor of the Course). Instructors review and grade student requirement documents, provide inline feedback, and configure AI assistance settings for their course section (when permitted). Team and document provisioning, and template selection, are reserved to the course admin (BR-3).

## **Student**

A user role enrolled in a course section as a member of a team. Students author requirement documents collaboratively with teammates, respond to instructor feedback, and submit work for grading.

## **MVP**

Minimum Viable Product. The smallest releasable version of the product that delivers usable value to stakeholders and validates core assumptions. In RAM, the MVP focuses on structured collaborative authoring with fixed built-in templates and the Socratic AI assistants that coach requirements authoring — the elicitation, critique, tutor, structuring, client role-play, and project assistants — reached through the project assistant. It excludes free-form AI generation of finished requirements (the drafting assistant is off by default in course use), template customization, advanced analytics and instructor dashboards, and automated/rubric grading.

## **FERPA**

Family Educational Rights and Privacy Act. A United States federal law that governs the privacy of student educational records. Systems handling such records must restrict access, control disclosure, and provide audit capabilities.

## **EARS**

Easy Approach to Requirements Syntax. A structured pattern language for writing functional requirements using clauses such as Ubiquitous, Event-Driven, State-Driven, Optional Feature, and Unwanted Behavior. RAM's FR-\* statements follow EARS conventions.

## **INVEST**

Independent, Negotiable, Valuable, Estimable, Small, Testable. A set of criteria for evaluating the quality of an individual requirement or user story. RAM's assistants and teaching context use INVEST as one of the standards against which student-authored requirements are critiqued.

## **Wiegers Requirement Categories**

The classification of requirements artifacts popularized by Karl Wiegers and Joy Beatty (e.g., business requirements, user requirements, functional requirements, quality attributes, constraints, business rules) and reflected in RAM's built-in templates. The teaching context references these categories so that assistant feedback aligns with the structure students are taught and graded against.

## **SSO**

Single Sign-On. An authentication mechanism that allows a user to access multiple applications with a single set of credentials. RAM relies on the host platform's (Project Pulse) institutional SSO provider for authentication.

## **LLM**

Large Language Model. A class of machine-learning models trained on broad text corpora to generate, complete, or analyze natural-language content. RAM integrates with an external LLM service (e.g., OpenAI) to power its assistants — the Socratic elicitation, critique, tutor, client role-play, structuring, and drafting roles that provide AI-assisted requirement guidance. All calls are routed through the server-side AI proxy.

## **WCAG**

Web Content Accessibility Guidelines. A set of internationally recognized standards published by the W3C for making web content more accessible to people with disabilities. RAM aims to conform to applicable WCAG levels (typically AA) for color contrast, keyboard navigation, screen-reader support, and similar concerns.
