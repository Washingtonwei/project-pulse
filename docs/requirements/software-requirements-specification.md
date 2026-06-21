# **Project Pulse**

# **Software Requirements Specification**

# **Version 1.0**

# **Revision History**

| Date          | Version | Description | Author   |
| ------------- | ------- | ----------- | -------- |
| \<dd/mmm/yy\> | \<x.x\> | \<details\> | \<name\> |
|               |         |             |          |
|               |         |             |          |
|               |         |             |          |

# **Introduction**

## **The Purpose of the Project**

**Project Pulse** is the web application for the Department of Computer Science's senior design / capstone course at TCU. It manages course sections, teams, students, and instructors, and delivers two capability areas: the weekly activity report and peer evaluation workflows that track student performance, and **Requirements Authoring & Management (RAM)**, a graph-first, model-driven requirements IDE. The performance-tracking workflows replace a manual spreadsheet-and-LMS process with an integrated system that collects submissions and compiles scores and feedback automatically. RAM exists because students today author requirements in generic, document-centric tools (Microsoft Word, Google Docs) that cannot model the structure, relationships, or semantics of requirements engineering; it instead lets student teams author, link, validate, trace, and review requirements as a connected graph of typed artifacts, supported by Socratic AI assistants that coach rather than author and by instructors who configure the teaching context and review submissions. The system's users are students (authoring and submitting in teams), instructors, and course admins (configuring, reviewing, and grading). The full business motivation — the problem and opportunity, business objectives, stakeholders, and scope — lives in the Vision and Scope document and is referenced here rather than repeated (see the Vision and Scope section and [vision-and-scope.md](vision-and-scope.md)).

## **The Purpose of this Document**

This Software Requirements Specification describes the external behavior and quality attributes of Project Pulse for release 1.0, across both of its capability areas — the weekly activity report and peer evaluation workflows and the Requirements Authoring & Management (RAM) environment. The behavioral specification for both areas is carried by the use cases ([use-cases.md](use-cases.md)), each of which is itself a high-level functional requirement. The non-use-case functional requirements, data model, external interfaces, and quality attributes in this document specify the system-level behaviors that fall outside any single use case; these are concentrated in the cross-cutting subsystems — authentication and access control, notifications, and the RAM-specific autosave, locking, validation, AI orchestration, export, and import — because the performance-tracking workflows are specified almost entirely as use cases. It is the reference against which Project Pulse is built, tested, and maintained, and it aligns students, instructors, and developers on what the system does.

**How the requirements are organized.** The requirements are not one document but a set of linked documents that describe a single shared model of the system from complementary angles, each the source of truth for its own topic. The SRS is the integrating entry point: it specifies the requirements it owns and links to the others rather than restating them, so each topic is defined once. The documents, in reading order:

- **Project Glossary** ([project-glossary.md](project-glossary.md)) — the domain vocabulary; the canonical definition of every term the other documents use.
- **Vision and Scope** ([vision-and-scope.md](vision-and-scope.md)) — the business motivation: problem and opportunity, business objectives, stakeholders and user classes, risks, assumptions, and the major features in and out of scope.
- **Use Cases** ([use-cases.md](use-cases.md)) — the behavioral specification: each user-initiated workflow as a use case, which is itself a high-level functional requirement.
- **Business Rules** ([business-rules.md](business-rules.md)) — the cross-cutting policies, constraints, and access rules (`BR-*`) that the use cases and this SRS enforce.
- **Software Requirements Specification** (this document) — the integrating specification: it orients the reader (the Overall Description section), then specifies the non-use-case functional requirements, the data model, external interfaces, and quality attributes, citing the documents above rather than repeating them.
- **Architectural Design** ([../design/architectural-design.md](../design/architectural-design.md)) — the single arc42/C4 architecture-of-record, sitting below this SRS: the shared software architecture, conventions, and deployment that the RAM module inherits, plus the component views for the foundation and performance-tracking features and for RAM, and cross-cutting subsystems.
- **Traceability** ([../traceability.md](../traceability.md)) — the spec→code map: one row per use case linking it to the requirements and design it realizes and the code and tests that implement it.

```mermaid
flowchart TB
    GLO["Project Glossary<br/>vocabulary"]
    VS["Vision and Scope<br/>why and scope"]
    UC["Use Cases<br/>behavior = high-level FRs"]
    BR["Business Rules<br/>policies (BR-*)"]
    SRS["Software Requirements Specification<br/>integrating entry point"]
    AD["Architectural Design<br/>architecture, deployment"]
    TR["Traceability<br/>UC to design to code to tests"]

    GLO --> SRS
    VS --> SRS
    UC --> SRS
    BR --> SRS
    SRS --> AD
    UC --> TR
    AD --> TR
```

The arrows read "is referenced by": the Project Glossary, Vision and Scope, Use Cases, and Business Rules documents are integrated by this SRS; the Architectural Design document sits below the SRS; and Traceability maps each use case, through the design, to the code and tests that realize it.

## **Document Conventions**

- Identifier schemes are stable, append-only handles, independent of heading position. Non-use-case functional requirements use `FR-<AREA>-<n>` (e.g., `FR-LOCK-2`); use cases use `UC-<AREA>-<n>` ([use-cases.md](use-cases.md)); business rules use `BR-<n>` ([business-rules.md](business-rules.md)). Product-generated artifact keys (e.g., `BO-3`, `RI-1`, `AS-6`, `UC-5`) follow a per-type running sequence, unique within a team, as defined under artifact key in the Project Glossary. SRS-local identifiers label its interface, data, and quality items: the External Interface Requirements codes (`UI-*`, `SI-*`, `CI-*`), the Data Requirements codes (`DI-*`), and the Quality Attributes codes (`USE-*`, `PER-*`, `SEC-*`, `SAF-*`, `AVL-*`, `ROB-*`, `SCA-*`, `INT-*`, `MNT-*`). The operating environment (`OE-*`), design and implementation constraints (`CO-*`), and architecture-level assumptions and dependencies (`AS-*`, continuing the sequence begun in Vision and Scope, and `DE-*`) are defined in the Overall Description section below.
- Non-use-case functional requirements are written as EARS-style "shall" statements. A use case is itself a high-level functional requirement, so its steps and Associated Information are its detailed specification and are not restated as separate functional requirements.
- Markdown is the canonical format and cross-references are live links. Square-bracketed italic passages are template author-guidance, not requirements.
- This document does not duplicate content owned by another document: each topic has a single source of truth and is referenced here (for example, the Project Glossary, Vision and Scope, Use Cases, and Business Rules documents are linked here rather than copied in).

## **References**

- Project Glossary: [project-glossary.md](project-glossary.md)
- Vision and Scope: [vision-and-scope.md](vision-and-scope.md)
- Use Cases: [use-cases.md](use-cases.md)
- Business Rules: [business-rules.md](business-rules.md)
- Architectural Design (architecture-of-record): [../design/architectural-design.md](../design/architectural-design.md)
- User Interface Wireframe/Prototypes: URL (N/A)
- API Document: [RAM API](https://app.swaggerhub.com/apis/Washingtonwei/RAM/1.0.0)

# **Overall Description**

This section orients the reader to Project Pulse's context, users, environment, and the constraints and assumptions under which it is built. Where another document is the source of truth — the product positioning and stakeholder profiles in Vision and Scope, the architecture views in the Architectural Design document — this section points to it rather than restating it. The operating environment, design and implementation constraints, and architecture-level assumptions and dependencies are specified here because requirements throughout this SRS — in Data Requirements, External Interface Requirements, and Quality Attributes — cite them by ID.

## **Product Perspective**

Project Pulse is a single web application that delivers two capability areas over one shared architecture — a Vue.js single-page application, a Java/Spring Boot REST API, and a relational database, with one authentication mechanism and one notification service. The first capability area is the weekly activity report and peer evaluation workflows that track student performance; the second is the Requirements Authoring & Management (RAM) environment, a module within the same application rather than a parallel system, reusing that shared single-page application, REST API, database, authentication, and notifications. The system context and container views are in the architecture-of-record ([Context and Scope](../design/architectural-design.md#context-and-scope), [Containers](../design/architectural-design.md#containers)); the product positioning and competitive alternatives are in Vision and Scope ([Product Perspective](vision-and-scope.md#product-perspective)).

## **User Classes and Characteristics**

Project Pulse has three user classes — student, instructor, and course admin — profiled in Vision and Scope ([Stakeholder Profiles and User Descriptions](vision-and-scope.md#stakeholder-profiles-and-user-descriptions)). Students are the favored user class: where their needs conflict with another class's, the student's learning outcome governs (consistent with the educational-value priority that drives the AI assistant design).

## **Operating Environment**

OE-1: Project Pulse shall run in the current released versions of Google Chrome, Mozilla Firefox, Microsoft Edge, and Apple Safari.

OE-2: The Project Pulse server shall run the REST API as a Java/Spring Boot application on a supported Java Virtual Machine, serve the Vue.js single-page application to clients, and persist data in a relational database.

OE-3: Users shall access RAM over HTTPS from the public internet, requiring no client software beyond a web browser.

OE-4: Project Pulse shall be deployed as a single application in which both capability areas — the weekly activity report and peer evaluation workflows and the RAM environment — share one single-page application, one REST API, and one database.

OE-5: Project Pulse shall reach the external LLM service over HTTPS and the Gmail system over SMTP for AI-assisted review and email notifications, respectively.

## **Design and Implementation Constraints**

CO-1: Project Pulse shall be a single application sharing one Vue.js single-page application, one Java/Spring Boot REST API, and one relational database across both capability areas; the RAM environment shall be implemented as a module within that codebase rather than as a separate system.

CO-2: The client shall be implemented in Vue.js and the backend in Java using the Spring Boot framework.

CO-3: Requirement artifacts, links, documents, and document sections shall be persisted in the Project Pulse relational database.

CO-4: Project Pulse shall provide a single JWT-based authentication mechanism for all users across both capability areas; the RAM environment shall reuse it rather than implementing a separate login.

CO-5: Project Pulse shall comply with FERPA when storing and transmitting student educational records.

CO-6: All calls to the external LLM service shall be routed through the REST API's AI proxy so that service credentials remain server-side and are never exposed to the browser.

CO-7: Email notifications shall be sent through the Gmail SMTP integration.

## **Assumptions and Dependencies**

Assumptions and dependencies are graph artifacts with team-wide key sequences (`AS-*`, `DE-*`) unique within a team (BR-5); documents are views over one shared graph, so the keys do not restart per document. The business-level assumptions `AS-1`…`AS-5` are in Vision and Scope ([Business Assumptions and Dependencies](vision-and-scope.md#business-assumptions-and-dependencies)); the architecture-level assumptions below continue that same `AS-*` sequence.

AS-6: Users have a supported web browser and a reliable internet connection.

AS-7: The external LLM service (e.g., OpenAI) remains available and its API contract stays stable for the integration RAM relies on.

AS-8: Project Pulse's course, course section, team, and user data is accurate and current; the RAM environment reads this shared data rather than maintaining its own copy.

DE-1: The RAM environment depends on the rest of Project Pulse for authentication, the course/course section/team data model, and the course admin, instructor, and student roles.

DE-2: AI-assisted requirement review depends on the external LLM service; if it is unavailable, the AI features are unavailable while the rest of Project Pulse continues to operate.

DE-3: Email notifications depend on the Gmail SMTP integration.

# **Project Glossary**

The Project Glossary is available here: [project-glossary.md](project-glossary.md).

# **Vision and Scope**

The Vision and Scope document is available here: [vision-and-scope.md](vision-and-scope.md).

# **Functional Requirements**

## **Use Cases**

The Use Cases document is available here: [use-cases.md](use-cases.md).

Each **use case is itself a functional requirement**, expressed at a high level: it states a user goal and the system's behavior in achieving it. Within a use case, the individual steps whose subject is "the system" — together with the use case's **Associated Information** (validation rules, duplication rules, search and display strategies, deletion strategies, notifications, and the like) — are the **finer-grained functional requirements** that detail that behavior. The use cases therefore carry the full functional specification for every user-initiated workflow, and those requirements are deliberately **not restated** here; doing so would duplicate the specification and create two copies to keep in sync. The Non-Use Case Functional Requirements section below complements the use cases by capturing the remaining functional behaviors that are _not_ user-initiated workflows (system-driven, event-driven, global, or background). Together, the use cases and the non-use-case functional requirements define the complete set of functional requirements for Project Pulse, including a small number of capabilities deferred to a future release, which are labeled as such.

## **Non-Use Case Functional Requirements**

Not all functional behaviors of Project Pulse are best expressed as use cases. This section captures system-driven, event-driven, global, or background behaviors using structured "shall" statements following principles inspired by the EARS (Easy Approach to Requirements Syntax) format.

These requirements describe system-level functions that support or enable the use cases but are not user-initiated workflows. They span both capability areas: security and authorization (`FR-SEC-*`) and notifications (`FR-NOT-*`) are system-wide, while autosave, locking, validation, real-time collaboration, AI orchestration, templates, terminology invariants, authorship metadata, export, and import are the cross-cutting subsystems of the RAM environment.

### *Autosave and Persistence Requirements*

**FR-SAVE-1 (State-Driven):** While a student is actively editing an authoring destination, the system shall automatically save the authoring destination's content at least every 10 seconds.

**FR-SAVE-2 (Event-Driven):** When a student leaves an authoring destination or navigates away, the system shall immediately persist the latest content of that authoring destination.

**FR-SAVE-3 (Ubiquitous):** The system shall ensure that no more than 10 seconds of work is lost in the event of a browser crash, disconnection, or power failure.

**FR-SAVE-4 (Event-Driven):** When an autosave operation fails, the system shall notify the user and retry in the background without interrupting editing.

### *Authoring Destination Locking Requirements*

**FR-LOCK-1 (Event-Driven):** When a student begins editing an authoring destination, the system shall acquire an exclusive lock for that authoring destination for that student.

**FR-LOCK-2 (Ubiquitous):** The system shall prevent other students from modifying an authoring destination that is currently locked.

**FR-LOCK-3 (Event-Driven):** When a student stops interacting with an authoring destination, the system shall release the lock for that authoring destination.

**FR-LOCK-4 (State-Driven Timeout):** While an authoring destination lock has been held for longer than a configurable interval since it was acquired (default 15 minutes), the system shall automatically release the lock.

**FR-LOCK-5 (Event-Driven):** When a lock is acquired or released, the system shall broadcast the updated lock state to all team members viewing the document.

### *Real-Time Collaboration Requirements*

*Post-MVP. Real-time collaboration — live collaborator presence and broadcast — is deferred beyond the initial release; the `FR-COL-*` requirements below (and the related PER-1 and ROB-3 targets) are retained as future scope. The MVP collaboration model is comment threads (UC-COL-2, UC-COL-3) over pessimistic section-level locking (`FR-LOCK-*`); concurrent authoring is serialized by locks rather than merged live.*

**FR-COL-1 (State-Driven):** While multiple students are connected to the same document, the system shall display the presence of each connected collaborator and the current lock state of each document section and use case.

**FR-COL-2 (Event-Driven):** When a collaborator joins a document, the system shall notify all currently connected users.

**FR-COL-3 (Event-Driven):** When a collaborator disconnects, the system shall notify all currently connected users within 2 seconds.

**FR-COL-4 (Ubiquitous):** The system shall ensure that real-time updates do not overwrite or corrupt content saved by other collaborators.

### *Validation and Consistency Requirements (ReqLint)*

**FR-VAL-1 (Ubiquitous):** The system shall provide a ReqLint validation engine that evaluates a requirement document against the applicable deterministic validation rules and produces a structured list of issues, each classified by severity (ERROR, WARNING, INFO) and tied to the document section or item it concerns. This engine is invoked both on student request (UC-VAL-1) and by the periodic background re-evaluation of FR-VAL-2.

**FR-VAL-2 (State-Driven, Optional):** While a student edits a document section, the system shall periodically re-evaluate the document section for ambiguity, missing required fields, or stylistic violations.

**FR-VAL-3 (Ubiquitous):** The system shall provide unique identifiers for all requirements, document sections, glossary terms, and use cases to support traceability.

**FR-VAL-4 (Event-Driven):** When a glossary term is renamed, the system shall identify and update or flag all references to that term across all documents in the project.

**FR-VAL-5 (Ubiquitous):** The system shall verify the presence of all mandatory document sections defined in the chosen template.

**FR-VAL-6 (Ubiquitous):** The system shall flag ambiguous, unverifiable, or subjective wording based on instructor-defined rules and defaults.

### *AI/LLM Integration Requirements*

RAM's AI assistance is delivered through Socratic assistants whose primary purpose is educational: to train students to author high-quality requirements rather than to hand them finished text. Where a design choice trades productivity against educational value, educational value governs.

**FR-AI-1 (Event-Driven):** When a student requests elicitation help, the system shall transmit to the elicitation assistant the context for the session's scope — for a session targeting a document section or use case, that target's context and the applicable template context; for a project-wide session, the project's current requirements coverage — together with the course section's teaching context, and shall return coaching for the student's own elicitation — candidate questions to put to the client (in plain, non-technical language), suggested follow-ups, and checks that help the student verify the client's answers — rather than finished requirement content.

**FR-AI-2 (Optional):** Where AI assistance is enabled, the critique assistant shall return its findings (possibly none) for clarity, consistency, completeness, or testability, each accompanied by an instructive rationale.

**FR-AI-3 (Ubiquitous):** The system shall not modify student-authored content with assistant-generated text without an explicit confirmation action by the student.

**FR-AI-4 (Event-Driven):** When a student requests an explanation for a flagged validation or critique issue, the tutor assistant shall return an explanation describing the rule or weakness involved and a suggested fix.

**FR-AI-5 (Ubiquitous):** The system shall visually distinguish assistant-generated suggestions from student-authored content until the student accepts them.

**FR-AI-6 (Ubiquitous):** The system shall include the course section's teaching context in the context provided to every assistant so that assistant feedback reflects the standards, common mistakes, and thinking order it defines.

**FR-AI-7 (State-Driven):** While an instructor has disabled a given assistant for a course section, the system shall make that assistant's corresponding feature unavailable to that course section's students; the drafting assistant shall be disabled by default.

**FR-AI-8 (Event-Driven):** When an assistant proposes concrete content, the system shall apply it only after an explicit, per-item acceptance by the student, and shall not provide an "accept all" action.

**FR-AI-9 (Ubiquitous):** The system shall accompany every assistant finding or proposal with an instructive rationale phrased for student learning.

**FR-AI-10 (Event-Driven):** When a student starts a practice client interview, the client role-play assistant shall respond in a non-technical client persona and shall not author requirements on the student's behalf.

**FR-AI-11 (Event-Driven):** When a student requests a review of planned client questions, the system shall flag technical jargon and suggest plain-language phrasings, each accompanied by a rationale.

**FR-AI-12 (Event-Driven):** When a student submits plain-language notes for translation, the structuring assistant shall propose candidate structured requirements, each traceable to its source note and applied only through the acceptance action of FR-AI-8.

**FR-AI-13 (State-Driven):** While the external LLM service is unavailable, the system shall make AI features unavailable and shall keep the rest of Project Pulse operational.

**FR-AI-14 (Ubiquitous):** Where a team has imported project source material, the system shall make it available to the AI assistants as context for elicitation, critique, and drafting.

**FR-AI-15 (Event-Driven):** When a student requests elicitation help, the elicitation assistant shall perform a gap analysis scoped to the session — for a session targeting a document section or use case, of that target against its template; for a project-wide session with no target selected, of the project's current requirements coverage across its documents and sections against what a complete set requires — and return candidate interview questions for the gaps it identifies.

**FR-AI-16 (Ubiquitous):** The system shall include each assistant's instructor-authored assistant instructions in the context provided to that assistant so that the assistant's role, persona, and boundaries reflect the instructor's per-assistant configuration.

**FR-AI-17 (Event-Driven):** When a student excludes the imported project source material for an elicitation session, the system shall ground that session's gap analysis solely on the team's current drafted requirements and shall omit the project source material from the elicitation assistant's context for that session.

**FR-AI-18 (Event-Driven):** When a student asks the project assistant for help, the system shall transmit the project's current requirements coverage, the imported project source material, and the course section's teaching context to the project assistant, and shall return orientation, answers about project status and coverage, navigation to the relevant document or artifact, and recommended next actions, without authoring requirement content.

**FR-AI-19 (Event-Driven):** When the project assistant recommends a specialized assistant or an authoring action, the system shall route the student into the corresponding use case and shall honor that assistant's per-course-section enablement.

**FR-AI-20 (Event-Driven):** When the drafting assistant is enabled and a student requests a draft, the system shall return a structural skeleton or clearly-marked candidate requirements derived from the student's prompt, applied only through the acceptance action of FR-AI-8.

**FR-AI-21 (Event-Driven):** When a student requests a whole-project review, the critique assistant shall evaluate the team's requirement documents together and return cross-document findings — covering completeness gaps, coverage and traceability holes, inconsistencies, and conflicts across the documents — each accompanied by an instructive rationale, and shall author no requirement content.

**FR-AI-22 (Ubiquitous):** The system shall include the course section's cross-document review criteria in the context provided to the critique assistant for a whole-project review, so that the review applies the instructor-configured criteria.

**FR-AI-23 (State-Driven):** While a course section's cross-document review criteria are undefined, the system shall make the whole-project review unavailable to that course section's students.

### *Template and Standards Enforcement Requirements*

The initial release ships fixed, built-in templates, and the enforcement requirements (FR-TPL-1, FR-TPL-3) apply to them. Template customization — letting a course admin or instructor author or edit templates (FR-TPL-2) — is **deferred to a future release and is not part of the MVP scope** (see Vision and Scope, [Template Management](vision-and-scope.md#template-management)). FR-TPL-2 is retained here, with its ID, so the intent is not lost.

**FR-TPL-1 (Ubiquitous):** The system shall enforce the structure, required document sections, and metadata defined by the active template (e.g., Wiegers, IEEE, RUP, custom).

**FR-TPL-2 (Deferred — future release):** When an authorized user (a course admin or instructor) updates a template, the system shall apply the updated structure to new documents but shall not retroactively modify existing documents without that user's approval.

**FR-TPL-3 (Ubiquitous):** The system shall apply the numbering and section-key scheme defined by the active template to all document sections within a document.

### *Terminology and Glossary Requirements*

**FR-GLO-1 (Ubiquitous):** The system shall provide a single authoritative definition for each glossary term within a project.

**FR-GLO-2 (Event-Driven):** When a glossary term is created or updated, the system shall ensure that references in documents link to the term.

**FR-GLO-3 (State-Driven):** While a student is writing or editing text, the system shall suggest existing glossary terms when there is a match.

### *Authorship Metadata and Document Versioning Requirements*

Authorship metadata (FR-HIS-4) is in scope for the initial release and is relied on by the authoring use cases. Document versioning — checkpointing, restoring, and retaining prior versions (FR-HIS-1, FR-HIS-2, FR-HIS-3) — is **deferred to a future release and is not part of the MVP scope** (see Vision and Scope, [Major Features / Scope](vision-and-scope.md#major-features--scope); tracked as OI-4). The three deferred requirements are retained here, with their IDs, so the intent is not lost.

**FR-HIS-1 (Deferred — future release):** When a document section is saved, the system shall create a version checkpoint containing the user, timestamp, and diff.

**FR-HIS-2 (Deferred — future release):** The system shall allow authorized users to restore a previous version of a document section.

**FR-HIS-3 (Deferred — future release):** The system shall preserve historical versions for at least one academic term.

**FR-HIS-4 (Ubiquitous):** The system shall record, for every authored item (glossary term, requirement artifact, use case, document section, artifact link, and comment), the identity of its creator and creation timestamp and the identity of its last editor and last-modified timestamp. For a use case, this authorship metadata is carried by the use case's paired requirement artifact audit record; the use case's constituent main steps and extensions do not carry separate authorship metadata. _(Cross-cutting; relied on by the authoring use cases — the create, edit, rename, and resolve flows across the glossary, documents, artifacts, links, and comments.)_

### *Security and Authorization Requirements*

**FR-SEC-1 (Ubiquitous):** The system shall authenticate users via its JWT-based authentication mechanism before granting access to protected resources.

**FR-SEC-2 (Ubiquitous):** The system shall enforce role-based access control (student, instructor, course admin).

**FR-SEC-3 (Event-Driven):** When an unauthorized user attempts to access a protected resource, the system shall deny access and provide an appropriate error message.

### *Export and Formatting Requirements*

**FR-EXP-1 (Event-Driven):** When a user exports a document, the system shall generate a PDF, DOCX, or Markdown file consistent with the template-defined structure. _(Realizes UC-EXP-1; honors BR-1.)_

**FR-EXP-2 (Ubiquitous):** The system shall maintain table of contents, heading hierarchy, numbering, and formatting consistency in exported documents.

### *Project Source Material and Import Requirements*

**FR-IMP-1 (Event-Driven):** When a student imports client pitch materials, the system shall accept PDF (`.pdf`) and PowerPoint (`.pptx`, `.ppt`) files, reject any file whose type is not on this allowlist or whose size exceeds a configurable per-file size limit (default 25 MB), and store each accepted file as the team's project source material.

**FR-IMP-2 (Event-Driven):** When project source material is imported, the system shall extract its text content for reference and for use as assistant context, and shall report when extraction is incomplete (for example, for image-only or scanned files).

### *Performance and Reliability Requirements*

**FR-PERF-1 (State-Driven):** While up to 100 users are actively editing concurrently, the system shall propagate collaborator presence events (join, disconnect) within 1 second for 95% of events. _(Post-MVP — depends on the deferred real-time collaboration; see the Real-Time Collaboration Requirements section.)_

**FR-PERF-2 (Ubiquitous):** The system shall remain available at least 99% of the academic term.

### *Notification Requirements*

**FR-NOT-1 (Event-Driven):** When the system raises a review-workflow notification — a requirement document submitted for review, returned for revision, or accepted — it shall deliver it by email to the designated recipients through the Gmail SMTP integration. _(Supports UC-REV-1, UC-REV-2; honors DE-3.)_

**FR-NOT-2 (Ubiquitous):** The system shall not raise persistent or email notifications for routine authoring changes (creating, editing, or deleting glossary terms, requirement artifacts, artifact links, document sections, use cases, and comments); such changes are propagated to connected collaborators in real time per FR-COL-1..4 instead.

**FR-NOT-3 (Event-Driven):** On a course section's configured weekly due day for weekly activity reports or peer evaluations, the system shall email each student in that course section a submission reminder listing the item(s) due that day and their due times, delivered through the Gmail SMTP integration. The reminder job runs only for course sections eligible for reminders in the current week and may be disabled by configuration. _(Supports BO-9, BO-10.)_

# **Business Rules**

The Business Rules document is available here: [business-rules.md](business-rules.md). That catalog defines the `BR-*` identifiers cited by the Business Rules fields in the Use Cases document.

# **Data Requirements**

## **Business Domain Model**

Project Pulse's domain spans both capability areas, modeled below as two diagrams: the performance-tracking domain and the requirements graph that the RAM environment adds. The two share the course / course section / team / user backbone — RAM scopes its requirements content to those same teams (BR-1) — but are otherwise independent.

### *Performance-tracking domain*

This diagram covers the shared foundation — course sections and their teams, students, and instructors — together with the performance-tracking entities: the rubrics and criteria used for assessment, each student's weekly activity report, and the peer evaluations students submit about one another. It predates this specification; field-level detail lives with the schema in the design docs ([../design/](../design/)).

```mermaid
classDiagram
    class Rubric {
        Integer rubricId
        String rubricName
    }
    class Criterion {
        Integer criterionId
        String criterion
        String description
        Double maxScore
    }
    class Rating {
        Integer ratingId
        Double actualScore
    }
    class PeerEvaluation {
        Integer peerEvaluationId
        String week
        Double totalScore
        String publicComment
        String privateComment
    }
    class Student {
        Integer id
        String email
        String firstName
        String lastName
    }
    class Course {
        Integer courseId
        String courseName
        String courseDescription
    }
    class Section {
        Integer sectionId
        String sectionName
        LocalDate startDate
        LocalDate endDate
        List~String~ activeWeeks
        Boolean isActive
        DayOfWeek warWeeklyDueDay
        LocalTime warDueTime
        DayOfWeek peerEvaluationWeeklyDueDay
        LocalTime peerEvaluationDueTime
    }
    class Team {
        Integer teamId
        String teamName
        String description
        String teamWebsiteUrl
    }
    class Instructor {
        Integer id
        String email
        String firstName
        String lastName
    }
    class Activity {
        Integer activityId
        String week
        ActivityCategory category
        String activity
        String description
        Double plannedHours
        Double actualHours
        ActivityStatus status
        String comments
    }

    class ActivityCategory {
        <<enumeration>>
        DEVELOPMENT
        TESTING
        BUGFIX
        COMMUNICATION
        DOCUMENTATION
        DESIGN
        PLANNING
        LEARNING
        DEPLOYMENT
        SUPPORT
        MISCELLANEOUS
    }

    class ActivityStatus {
        <<enumeration>>
        IN_PROGRESS
        COMPLETED
    }

    Rubric "0..*" --> "0..*" Criterion
    Rating --> "1" Criterion
    PeerEvaluation -- "1..*" Rating
    PeerEvaluation --> "1" Student : evaluator
    PeerEvaluation --> "1" Student : evaluatee
    Section "1" <--> "0..*" Team
    Section "1" <--> "0..*" Student
    Section "0..*" -- "1" Rubric
    Team "1" <--> "0..*" Student
    Section "0..*" <--> "0..*" Instructor
    Team "0..*" <--> "1" Instructor
    Activity "0..*" --> "1" Student
    Activity "0..*" --> "1" Team
    Course "1" <--> "0..*" Section
    Course --> "1" Instructor : courseAdmin
    Course "1..*" <--> "0..*" Instructor
    Course "1" <--> "0..*" Rubric
    Course "1" <--> "0..*" Criterion
    Instructor --> "1" Course : defaultCourse
    Instructor --> "1" Section : defaultSection
```

Here `Section` is the course section entity — the enrollment unit students join and that groups them into teams — not a document section. There is no standalone weekly-activity-report entity: a student's weekly activity report is her `Activity` rows for a given `week`. A course section's `warWeeklyDueDay` / `peerEvaluationWeeklyDueDay` and the paired due times drive the weekly submission reminders (FR-NOT-3). `ActivityCategory` and `ActivityStatus` are enumerations (shown above); `DayOfWeek`, `LocalDate`, and `LocalTime` are `java.time` types. `Student` and `Instructor` share the platform's user identity fields (id, email, name).

### *RAM requirements graph*

The RAM environment persists requirements as a team-scoped graph of typed requirement artifacts connected by typed artifact links, surfaced through requirement documents and their document sections — directly realizing the graph-first model described in the Project Glossary and Vision and Scope. The entities fall into four groups: ownership and documents, the requirements graph, use-case structure, and collaboration.

**Ownership and documents**

- **Team** — the ownership boundary for all requirements content (per BR-1); a team owns its documents, artifacts, and links.
- **RequirementDocument** — a document of a given `DocumentType`, with a `documentKey`, `title`, and a `status` (`DRAFT` → `SUBMITTED` → `RETURNED` or `ACCEPTED`) that drives the review-and-submission workflow (BR-13, BR-14, UC-REV-\*): a submitted document is either returned for revision (`RETURNED`, editable again) or accepted (`ACCEPTED`, final and read-only).
- **DocumentSection** — a section of a document, identified by its `sectionKey` (the section key), with a `title`, a `type` (`RICH_TEXT` narrative or a `LIST` of artifacts), authored `content`, and template `guidance`.
- **DocumentSectionLock** — the exclusive edit lock held on a document section while a student edits it (`lockedAt`, `expiresAt` for the lock-expiry timeout, `reason`), realizing FR-LOCK\* and BR-9 / BR-10 for document-section authoring destinations.

**The requirements graph**

- **RequirementArtifact** — the atomic graph node: its `type` (`RequirementArtifactType`), `artifactKey` (the artifact key, e.g., `FR-1`, `UC-5`), `title`, `content`, `priority`, and `notes`. An artifact is grouped under a source `DocumentSection`; the section determines where the artifact appears in a requirement document.
- **ArtifactLink** — a typed, directed edge between two artifacts (the artifact's `outgoing` and `incoming` links): its `type` (`ArtifactLinkType`) and optional `notes`. This is the requirement link of the glossary.
- **ArtifactKeySequence** — a per-team, per-artifact-type sequence counter that assigns the next product-generated `artifactKey` while preserving the artifact-key uniqueness and stability rules (BR-5).
- Enumerations: `RequirementArtifactType` (the authoritative artifact taxonomy), `ArtifactLinkType` (`DERIVES_FROM`, `REALIZES`, `REFERENCES`, `IMPACTS`, `MITIGATES`, `MOTIVATES` — matching the glossary's artifact link type), and `Priority` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).

**Use-case structure**

- **UseCase** — the structured behavioral spec (`trigger`, plus its flows below), paired **1:1 with a RequirementArtifact** of type `USE_CASE` so a use case participates in the graph (links, traceability) like any other artifact while keeping its detailed behavioral fields.
- **UseCaseLock** — the exclusive edit lock held on a use case while a student edits it (`lockedAt`, `expiresAt` for the lock-expiry timeout, `reason`), realizing FR-LOCK\* and BR-9 / BR-10 for use-case authoring destinations.
- **UseCaseMainStep**, **UseCaseExtension**, **UseCaseExtensionStep** — the ordered decomposition of a use case's normal flow and its alternative/exception extensions and their steps. Preconditions and postconditions are represented as associated `RequirementArtifact` nodes of type `PRECONDITION` and `POSTCONDITION`, so they remain part of the requirements graph.

**Collaboration**

- **CommentThread** — a discussion (`status` `OPEN` / `RESOLVED`) attachable to a `RequirementDocument`, a `DocumentSection`, or a `RequirementArtifact`; realizes UC-COL-2 / UC-COL-3.
- **Comment** — one message within a thread (`content`).

```mermaid
classDiagram
direction TB
    class Team {
        -Integer teamId
    }

    class RequirementDocument {
        -Long id
        -DocumentType type
        -String documentKey
        -String title
        -DocumentStatus status
    }

    class DocumentSection {
        -Long id
        -String sectionKey
        -String title
        -SectionType type
        -String content
        -String guidance
    }

    class DocumentSectionLock {
        -Long id
        -Instant lockedAt
        -Instant expiresAt
        -String reason
    }

    class UseCaseLock {
        -Long id
        -Instant lockedAt
        -Instant expiresAt
        -String reason
    }

    class UseCase {
        -Long id
        -String useCaseTrigger
    }

    class UseCaseMainStep {
        -Long id
        -String actor
        -String actionText
    }

    class UseCaseExtension {
        -Long id
        -String label
        -String conditionText
        -ExtensionKind kind
        -ExtensionExit extensionExit
        -Integer resumeStepNo
    }

    class UseCaseExtensionStep {
        -Long id
        -String actor
        -String actionText
    }

    class RequirementArtifact {
        -Long id
        -RequirementArtifactType type
        -String artifactKey
        -String title
        -String content
        -Priority priority
        -String notes
    }

    class ArtifactLink {
        -Long id
        -ArtifactLinkType type
        -String notes
    }

    class ArtifactKeySequence {
        -Long id
        -RequirementArtifactType type
        -long nextNumber
    }

    class CommentThread {
        -Long id
        -CommentThreadStatus status
    }

    class Comment {
        -Long id
        -String content
    }

    class DocumentType {
        <<enumeration>>
        VISION_SCOPE
        USE_CASES
        USER_STORIES
        BUSINESS_RULES
        SRS
        GLOSSARY
    }

    class SectionType {
        <<enumeration>>
        RICH_TEXT
        LIST
    }

    class DocumentStatus {
        <<enumeration>>
        DRAFT
        SUBMITTED
        RETURNED
        ACCEPTED
    }

    class RequirementArtifactType {
        <<enumeration>>
        GLOSSARY_TERM
        BUSINESS_PROBLEM
        BUSINESS_OPPORTUNITY
        BUSINESS_OBJECTIVE
        SUCCESS_METRIC
        VISION_STATEMENT
        RISK
        ASSUMPTION
        DEPENDENCY
        STAKEHOLDER
        BUSINESS_RULE
        FUNCTIONAL_REQUIREMENT
        FEATURE
        USE_CASE
        PRECONDITION
        POSTCONDITION
        USER_STORY
        QUALITY_ATTRIBUTE
        EXTERNAL_INTERFACE_REQUIREMENT
        CONSTRAINT
        DATA_REQUIREMENT
        OPERATING_ENVIRONMENT
        OTHER
    }

    class ArtifactLinkType {
        <<enumeration>>
        DERIVES_FROM
        REALIZES
        REFERENCES
        IMPACTS
        MITIGATES
        MOTIVATES
    }

    class Priority {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        CRITICAL
    }

    class CommentThreadStatus {
        <<enumeration>>
        OPEN
        RESOLVED
    }

    class ExtensionKind {
        <<enumeration>>
        ALTERNATE
        EXCEPTION
    }

    class ExtensionExit {
        <<enumeration>>
        RESUME
        END_SUCCESS
        END_FAILURE
    }

    RequirementDocument "many" --> "1" Team
    RequirementDocument "1" <--> "many" DocumentSection
    DocumentSection "1" <--> "1" DocumentSectionLock
    DocumentSection "1" <--> "many" RequirementArtifact
    Team "1" --> "many" RequirementArtifact
    ArtifactLink "many" --> "1" Team
    ArtifactKeySequence "many" --> "1" Team
    RequirementArtifact "1" <--> "many" ArtifactLink : outgoing
    RequirementArtifact "1" <--> "many" ArtifactLink : incoming
    UseCase "1" --> "1" RequirementArtifact : artifact
    UseCase "many" --> "1" RequirementArtifact : primaryActor
    UseCase "many" <--> "many" RequirementArtifact : secondaryActors
    UseCase "many" <--> "many" RequirementArtifact : preconditions
    UseCase "many" <--> "many" RequirementArtifact : postconditions
    UseCase "1" <--> "1" UseCaseLock
    UseCase "1" <--> "many" UseCaseMainStep
    UseCaseMainStep "1" <--> "many" UseCaseExtension
    UseCaseExtension "1" <--> "many" UseCaseExtensionStep
    RequirementDocument "1" <--> "many" CommentThread
    DocumentSection "1" <--> "many" CommentThread
    RequirementArtifact "1" <--> "many" CommentThread
    CommentThread "1" <--> "many" Comment
```

**Mapping to the conceptual model.** `RequirementArtifact` is the glossary's requirement artifact and `artifactKey` its artifact key (the per-type running sequence `FR-1`, `UC-5`); `ArtifactKeySequence` implements the team-scoped running sequence for each artifact type; `ArtifactLink` / `ArtifactLinkType` are the requirement link / artifact link type; `DocumentSection.sectionKey` is the section key; `DocumentSectionLock` and `UseCaseLock` realize the locking rules (BR-9 / BR-10) for the two authoring destination types; `DocumentStatus` drives the review lock (BR-13); `RequirementArtifact` associations on `UseCase` represent the primary actor, secondary actors, preconditions, and postconditions; and `CommentThread` / `Comment` back the commenting use cases (UC-COL-2 / UC-COL-3).

**Notes.**

- The built-in templates that provision a team's documents and sections (UC-TPL-1) are the _provisioning_ layer and are not shown in this domain diagram; the MVP ships fixed, built-in templates.
- `RequirementArtifactType` is the authoritative artifact taxonomy and is reconciled one-to-one with the glossary's requirement artifact list. `OTHER` is an implementation fallback, not a domain concept. A single `RISK` type is the umbrella over business/adoption, technical/feasibility, and security/safety risks (the earlier `BUSINESS_RISK`/`RISK` pair was collapsed into `RISK`); `DEPENDENCY` is a tracked artifact.
- User stories are a **deferred** concept: the `USER_STORY` artifact type and the `USER_STORIES` `DocumentType` are retained so a future, optional User Stories document can be enabled without a schema change, but no User Stories document, template, or use case ships in the MVP.

**Artifact type → authoring home.** Each artifact type is authored in a specific document section, which determines where it appears in a document. In the MVP the student authors an artifact in the document section she is in, which fixes its type (UC-ART-3); the map below is the canonical placement (and the basis for the future requirements-graph "add by type" path):

| Artifact type                               | Document → Section                                                                                                                                                                             |
| ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `GLOSSARY_TERM`                             | Glossary → term list                                                                                                                                                                           |
| `BUSINESS_PROBLEM`, `BUSINESS_OPPORTUNITY`  | Vision and Scope → Business Opportunity/Problem Statement                                                                                                                                 |
| `BUSINESS_OBJECTIVE`, `SUCCESS_METRIC`      | Vision and Scope → Business Objectives                                                                                                                                                    |
| `VISION_STATEMENT`                          | Vision and Scope → Vision Statement                                                                                                                                                       |
| `RISK`                                      | Vision and Scope → Risks                                                                                                                                                                  |
| `ASSUMPTION`, `DEPENDENCY`                  | Vision and Scope → Business Assumptions and Dependencies (business-level) **and** SRS → Assumptions and Dependencies (architecture-level, e.g., `DE-1`/`DE-2`) — these two types are authored in either document's Assumptions-and-Dependencies section |
| `STAKEHOLDER`                               | Vision and Scope → Stakeholder Profiles                                                                                                                                                   |
| `FEATURE`                                   | Vision and Scope → Major Features / Scope                                                                                                                                                 |
| `USE_CASE`, `PRECONDITION`, `POSTCONDITION` | Use Cases → the use case (preconditions/postconditions are its constituents)                                                                                                                   |
| `BUSINESS_RULE`                             | Business Rules → the rule list                                                                                                                                                                 |
| `FUNCTIONAL_REQUIREMENT`                    | SRS → Functional Requirements                                                                                                                                                             |
| `EXTERNAL_INTERFACE_REQUIREMENT`            | SRS → External Interface Requirements                                                                                                                                                       |
| `DATA_REQUIREMENT`                          | SRS → Data Requirements                                                                                                                                                                     |
| `QUALITY_ATTRIBUTE`                         | SRS → Quality Attributes                                                                                                                                                                    |
| `CONSTRAINT`                                | SRS → Design and Implementation Constraints (CO-\*)                                                                                                                                            |
| `OPERATING_ENVIRONMENT`                     | SRS → Operating Environment (OE-\*)                                                                                                                                                            |
| `USER_STORY`                                | _(deferred — a future, optional User Stories document)_                                                                                                                                        |
| `OTHER`                                     | _(fallback — no fixed home)_                                                                                                                                                                   |

## **Data Dictionary**

The Business Domain Model above names the system's entities, their fields, and their enumerations. Field-level definitions — data type, length, format, required/optional, and allowed values — are maintained with the database schema in the design docs ([../design/](../design/)), not restated here, so that a single source defines each field and the SRS does not drift from the implementation. Format-bearing fields that are themselves requirements (the `artifactKey`, `sectionKey`, and `documentKey` schemes and the document/comment status values) are specified where they are introduced: artifact key, section key, and the artifact-key uniqueness and stability rules in the Project Glossary and Business Rules (BR-5, BR-12), and the `DRAFT` → `SUBMITTED` → `RETURNED` status values in the Business Domain Model above.

## **Reports**

The performance-tracking capability generates reports: peer evaluation reports for students and instructors (UC-EVA-2, UC-EVA-3, UC-EVA-4) and weekly activity report summaries for teams and individual students (UC-WAR-2, UC-WAR-3); each is specified by its use case, including its report parameters and generating algorithm. The RAM environment generates no reports in release 1.0 — completeness-metric, progress, and requirement-quality dashboards over a team's requirements graph are a deferred RAM capability (future release). Document export — PDF, DOCX, or Markdown rendered to the template structure — is a formatted document, not a report, and is specified under External Interface Requirements (SI-3, FR-EXP-1, FR-EXP-2).

## **Data Acquisition, Integrity, Retention, and Disposal**

DI-1: RAM shall acquire user identity, role, course section, team membership, and team ownership data from the rest of Project Pulse rather than maintaining a separate copy (DE-1, SI-2).

DI-2: RAM shall persist requirement documents, document sections, requirement artifacts, artifact links, use cases, locks, comment threads, comments, and artifact-key sequences in the Project Pulse relational database (CO-3, SI-2.3).

DI-3: RAM shall scope persisted RAM content by team wherever the entity represents team-owned requirements content, and shall enforce that students can access only their own team's requirements graph, documents, project source material, comments, and locks (BR-1, FR-SEC-2).

DI-4: RAM shall assign product-generated artifact keys from the team's `ArtifactKeySequence` for the artifact type, shall keep assigned artifact keys stable across edits, and shall not reuse keys after deletion (BR-5, BR-12).

DI-5: RAM shall preserve graph integrity by preventing deletion of glossary terms or requirement artifacts while active artifact links or references still depend on them, unless those references are removed or repointed first (BR-12).

DI-6: RAM shall retain logically deleted glossary terms and requirement artifacts for audit, excluding them from normal active authoring and search results while preserving their identifiers and audit metadata (BR-12).

DI-7: RAM shall record authorship metadata for authored RAM items as specified by FR-HIS-4; known implementation gaps are tracked in OI-19.

DI-8: RAM shall use optimistic version fields and exclusive edit locks for document sections and use cases to protect concurrent edits, and shall treat expired locks as releasable according to the locking requirements (FR-LOCK-1..5).

DI-9: RAM shall not retain document-section version checkpoints for release 1.0; document version history is deferred to a future release (FR-HIS-1, FR-HIS-2, FR-HIS-3).

DI-10: RAM shall rely on the Project Pulse database backup, recovery, and disposal policies for physical retention and disposal of persisted RAM data, except where RAM-specific business rules require stronger logical retention for audit.

# **External Interface Requirements**

## **User Interfaces**

UI-1: Project Pulse is delivered as a single Vue.js single-page application; the RAM environment's user interface shall be a set of views within it, conforming to the application's established layout, navigation, and styling conventions (per CO-1, CO-2, INT-1). Detailed UI design is maintained with the SPA, not in this document.

UI-2: Project Pulse shall conform to WCAG 2.1 Level AA for color contrast, keyboard navigation, and screen-reader support (per USE-1; addresses risk RI-6).

UI-3: The requirement-document editor shall present a two-column layout — a document-and-section outline alongside the selected section's editor — with per-section locking; a list section shall provide an "Add Requirement" action for authoring artifacts within it. The Use Cases document editor shall expose equivalent per-use-case locking when a student edits a use case.

## **Software Interfaces**

SI-1: External LLM Service (via the AI proxy)

SI-1.1: RAM shall call the external LLM service only through the REST API's server-side AI proxy; the Vue single-page application shall never call the LLM service directly (per CO-6, SEC-4).

SI-1.2: For each assistant request, the AI proxy shall send the assembled assistant context — the assistant's system prompt, the course section's teaching context and per-assistant assistant instructions, the relevant document and requirements-graph content, and, where enabled, the team's project source material — and shall return the assistant's response to the requesting feature (per FR-AI-6, FR-AI-14, FR-AI-16).

SI-1.3: Requests to and responses from the LLM service shall use JSON; an assistant that returns candidate artifacts shall use a structured (tool/JSON) schema so the response is machine-parseable.

SI-1.4: LLM service credentials shall be held server-side and shall never be exposed to the browser (per CO-6, SEC-4).

SI-1.5: While the LLM service is unavailable or a request times out, the AI proxy shall report the condition to the requesting feature so that AI features become unavailable while the rest of Project Pulse continues to operate (per FR-AI-13, AVL-2, PER-4).

SI-2: Project Pulse shared foundation

SI-2.1: The RAM environment shall obtain the authenticated user's identity and role (course admin, instructor, student) from Project Pulse's authenticated session and shall not implement its own login (per CO-4, SEC-1).

SI-2.2: The RAM environment shall read course, course section, team, and membership data from Project Pulse's existing data model rather than maintaining its own copy (per DE-1; AS-8).

SI-2.3: The RAM environment shall persist its requirements graph — artifacts, links, documents, document sections, locks, comment threads, and comments — in the Project Pulse relational database (per CO-3).

SI-2.4: The RAM environment shall send email notifications through Project Pulse's Gmail SMTP integration (per CO-7, DE-3); the triggering conditions and message content are specified in the Communications Interfaces section.

SI-3: Document export

SI-3.1: RAM shall generate an exported document as a PDF, DOCX, or Markdown file consistent with the template-defined structure (per FR-EXP-1; realizes UC-EXP-1), and shall package all of a team's documents as a single bundle on request (UC-EXP-2).

SI-3.2: Exported documents shall preserve table of contents, heading hierarchy, numbering, and formatting consistency (per FR-EXP-2).

SI-4: Project source material import

SI-4.1: RAM shall accept PDF (`.pdf`) and PowerPoint (`.pptx`, `.ppt`) uploads as project source material and shall reject any file whose type is not on this allowlist or whose size exceeds a configurable per-file limit (default 25 MB) (per FR-IMP-1; realizes UC-AI-1).

SI-4.2: RAM shall extract the text content of an imported file for use as assistant context and shall report when extraction is incomplete, for example for image-only or scanned files (per FR-IMP-2).

## **API Document**

The API document is available on SwaggerHub: [RAM API](https://app.swaggerhub.com/apis/Washingtonwei/RAM/1.0.0).

## **Hardware Interfaces**

No hardware interfaces have been identified.

## **Communications Interfaces**

CI-1: RAM shall send review-workflow email notifications — when a requirement document is submitted for review, returned for revision, or accepted — to the designated recipients through Project Pulse's Gmail SMTP integration (per CO-7, DE-3, FR-NOT-1; supports UC-REV-1, UC-REV-2).

CI-2: RAM shall not send email for the routine authoring changes covered by FR-NOT-2; such changes propagate to connected teammates in real time over the collaboration channel instead (per FR-COL-1..4).

CI-3: RAM shall communicate with the external LLM service over HTTPS (per OE-5, SEC-4).

CI-4: Project Pulse shall conduct all browser-to-server communication over HTTPS.

# **Quality Attributes**

These quality attributes apply to Project Pulse as a whole. Some — accessibility, security, availability, and transport security — are system-wide; others name behaviors specific to the RAM environment's features (autosave and edit-loss bounds, ReqLint and AI response times, real-time collaboration) and are scoped to RAM accordingly.

## **Usability**

USE-1: Project Pulse shall follow WCAG 2.1 Level AA guidelines for color contrast, keyboard navigation, and screen-reader support. (Addresses risk RI-6.)

USE-2: Project Pulse shall be fully operable using the keyboard alone, including all RAM authoring, navigation, validation, and review actions.

USE-3: RAM shall present every ReqLint validation finding and AI critique finding with the specific document location it refers to and an instructive rationale, so that a student can act on it without external help.

USE-4: A new student shall be able to open a document section, edit and save content, run validation, and submit for review during her first session without prior training. _(Target: 95% of new students succeed without assistance in usability testing.)_

## **Performance**

PER-1: While up to 100 users are editing concurrently, RAM shall propagate collaborator presence and lock-state events (join, disconnect, lock acquire/release) within 1 second for 95% of events. (Realized by FR-PERF-1.) _(Post-MVP — depends on the deferred real-time collaboration; see the Real-Time Collaboration Requirements section.)_

PER-2: RAM shall autosave an actively edited authoring destination at least every 10 seconds and persist its latest content immediately when the student navigates away. (Realized by FR-SAVE-1, FR-SAVE-2.)

PER-3: RAM shall return ReqLint validation results for a single requirement document within 3 seconds for 95% of runs.

PER-4: RAM shall present an AI assistant response, or a clear "working" / timeout indication, within 15 seconds of a student's request.

## **Security**

SEC-1: Project Pulse shall authenticate every user through its JWT-based authentication mechanism before granting access to any protected resource, per CO-4 and FR-SEC-1.

SEC-2: Project Pulse shall enforce role-based access control across the course admin, instructor, and student roles, and the RAM environment shall restrict each student to her own team's requirements graph, documents, and project source material, per BR-1, BR-2, and FR-SEC-2.

SEC-3: Project Pulse shall store and transmit student educational records in compliance with FERPA, per CO-5.

SEC-4: RAM shall route all calls to the external LLM service through the server-side AI proxy and shall never expose LLM service credentials to the browser, per CO-6.

SEC-5: Project Pulse shall encrypt all traffic between the browser and the server over HTTPS, per OE-3.

## **Safety**

SAF-1: RAM is a web-based requirements-authoring tool with no physical actuation or safety-critical functions; no safety hazards have been identified, and no safety requirements apply.

## **Availability**

AVL-1: Project Pulse shall be available at least 99% of each academic term, excluding scheduled maintenance windows, with availability prioritized near assignment deadlines. (Realized by FR-PERF-2.)

AVL-2: While the external LLM service is unavailable, Project Pulse shall keep all non-AI functionality operational and clearly indicate that AI assistance is temporarily unavailable, per FR-AI-13 and DE-2.

## **Robustness**

ROB-1: In the event of a browser crash, disconnection, or power failure, RAM shall lose no more than 10 seconds of a student's edits. (Realized by FR-SAVE-3.)

ROB-2: When an autosave fails, RAM shall notify the student and retry in the background without interrupting editing. (Realized by FR-SAVE-4.)

ROB-3: RAM shall ensure that real-time collaborative updates never overwrite or corrupt content already saved by another collaborator, per FR-COL-4 and BR-11. _(Post-MVP — applies to the deferred real-time collaboration; in the MVP, section-level locking prevents concurrent overwrites.)_

## **Scalability and Interoperability**

SCA-1: Project Pulse shall sustain its performance and availability targets under peak concurrent load near assignment deadlines for a Senior Design cohort of approximately 70 students (about 75 total users including instructors and course admins), whose peak concurrent editing stays within the 100-concurrent performance envelope specified in PER-1 (cf. risk RI-3).

INT-1: Project Pulse shall operate as a single application across both capability areas; the RAM environment shall reuse its single-page application, REST API, relational database, authentication, and notification services rather than introducing a parallel system, per CO-1 and OE-4.

## **Maintainability**

MNT-1: RAM shall access its requirements graph behind a service layer so that new artifact types and artifact link types can be added without reworking unrelated features, consistent with its module structure within Project Pulse (per CO-1, INT-1).

**Priority of attributes:** where quality attributes conflict, the intended priority order is **security and data integrity → availability → usability → performance**. Educational value governs trade-offs in the AI assistant features specifically (see [AI/LLM Integration Requirements](#aillm-integration-requirements)).

# **Internationalization and Localization Requirements**

N/A for release 1.0. Project Pulse targets TCU software-engineering courses and ships in U.S. English only; no multi-language, multi-currency, or locale-specific formatting requirements apply. (Accessibility is in scope but is a usability requirement — see USE-1 / WCAG 2.1 AA in the Usability section — not an internationalization one.)

# **Other Requirements**

Project Pulse introduces no additional requirements beyond those specified elsewhere; the cross-cutting concerns that would otherwise appear here are referenced rather than restated:

- Regulatory and compliance: FERPA handling of student educational records — CO-5 and SEC-3 (Security).
- Security and access control: platform authentication and role-based access — FR-SEC-1..3 (Security and Authorization Requirements) and the Security quality attributes.
- Authorship and audit trail: creator/editor identity and timestamps on every authored item — FR-HIS-4 (Authorship Metadata and Document Versioning Requirements); logical (soft) deletion that retains items for audit — BR-12.
- Installation, configuration, and startup/shutdown: Project Pulse is deployed as a single application (see the [Deployment View](../design/architectural-design.md#deployment-view)).
