# **Project Pulse**

# **Vision and Scope**

# **Version 1.0**

# **Revision History**

| Date          | Version | Description | Author   |
| ------------- | ------- | ----------- | -------- |
| \<dd/mmm/yy\> | \<x.x\> | \<details\> | \<name\> |
|               |         |             |          |
|               |         |             |          |
|               |         |             |          |

# **Introduction**

This document defines the purpose, goals, and boundaries of **Project Pulse**, the web application that supports the Department of Computer Science's senior design / capstone course at Texas Christian University. Project Pulse serves the course end to end through two complementary capability areas:

- **Course operations and student performance tracking** — managing course sections, teams, students, and instructors, and replacing the manual, spreadsheet-based collection of weekly activity reports (WARs) and peer evaluations with an integrated, automated workflow.
- **Requirements Authoring & Management (RAM)** — a web-based requirements authoring environment that modernizes how software requirements are written, understood, and evaluated, replacing unstructured, document-centric requirement writing with a structured, model-driven approach that reflects professional requirements engineering practices while remaining accessible to students and instructors.

The two areas share the same users, the same course / course section / team model, and a single deployment. The remainder of this section describes the need each area addresses: the performance-tracking workflow first, then the requirements-authoring environment that the rest of this document treats in depth.

**Student performance tracking.** In the senior design course, students work in teams with real clients, and contribution naturally varies across a team. Project Pulse's weekly activity reports give each student a structured place to record what they did each week, and its peer evaluations let teammates assess one another against a rubric — together fostering communication, self-awareness, and accountability. These workflows were previously run by hand with shared Google Sheets and Excel spreadsheets uploaded to the university LMS; Project Pulse brings them into one system and automates compilation, scoring, and reporting. This part of the platform is established and in production; the requirements-authoring environment described next is the area this document specifies in depth.

**Requirements authoring.** Traditional requirement authoring tools such as Microsoft Word and Google Docs treat requirements as free-form text embedded in static documents. While familiar, these tools do not enforce structure, terminology consistency, traceability, or writing standards—core principles of effective requirements engineering. As a result, students often produce incomplete, inconsistent, and difficult-to-maintain specifications, and instructors must expend significant effort reviewing and grading long, low-quality documents.

At the core of the RAM tool is the concept of atomic requirement artifacts. Rather than treating requirements as narrative prose, RAM models each meaningful requirement element as a first-class, uniquely identifiable artifact with a well-defined type and lifecycle. Examples of atomic requirement artifacts include problems, business objectives, product features, use cases, use case steps and extensions, functional requirements, business rules, constraints, risks, and glossary terms. These artifacts are explicitly linked to one another to represent traceability relationships such as refinement, realization, dependency, and reference.

Standard requirement documents—such as Vision and Scope, Glossary, Use Cases, Business Rules, and the Software Requirements Specification (SRS)—are presented in RAM as structured views over a shared set of atomic requirement artifacts, rather than as isolated text files. This abstraction enables capabilities that are difficult or impossible in document-centric tools, including end-to-end traceability across requirement levels, navigation and visualization of requirement relationships, safe refactoring (e.g., renaming a glossary term updates all references), impact analysis, and automated validation of completeness and consistency.

RAM is designed specifically for software engineering, senior design, and capstone courses, where students must learn not only what requirements to write, but how to write them clearly, consistently, and professionally. By enforcing structure, modeling relationships explicitly, and guiding students toward atomic, specification-grade requirements, RAM supports both improved learning outcomes and more efficient, consistent instructor evaluation.

This document outlines the business opportunity, major features, stakeholders, and proposed workflows. Detailed behavioral specifications, cross-cutting business rules, and implementation-facing requirements are defined in the Use Cases, Business Rules, and Software Requirements Specification (SRS) documents.

## **Background**

Requirements are the foundation of all software development efforts, describing what the system must do, how it behaves, and any constraints under which it must operate. Requirements engineering is a core competency in software engineering and is essential for successful senior design and capstone projects.

Furthermore, as large language models (LLMs) become increasingly capable of generating code, clear and unambiguous requirements are more important than ever. In AI-aided software engineering, the quality of requirements strongly shapes the quality of AI-generated solutions. Poorly specified requirements lead to misinterpretation, unintended behavior, and incorrect implementations.

In academic settings, students typically write requirements using Microsoft Word or Google Docs based on instructor-provided templates. While familiar, these general-purpose editors do not enforce structure, terminology consistency, writing standards, validation rules, or traceability—core principles of professional requirements engineering.

The Department of Computer Science at TCU teaches a two-semester Software Engineering / Senior Design sequence in which students must deliver professional-grade requirements for real clients. RAM is designed to support this educational process with structure, intelligence, and explicit modeling of requirements and their relationships.

## **Current Process Flows (As-Is Process Flows)**

**Weekly activity reports and peer evaluations.** A student-performance tracking process is already in place, run manually with spreadsheets:

- *Weekly activity report (WAR).* Every student completes a weekly activity report each week recording what they did. WARs are kept in shared Google Sheets — one sheet per team per week — that teammates can edit and view, which also fosters communication. Each Monday students record the previous week's activities in their team's sheet; the instructor keeps the URLs of all the sheets, reviews the updated ones on Tuesday, and grades and gives feedback through the university LMS (TCU Online).
- *Peer evaluation.* Each Tuesday students first review the team's WAR from the previous week, complete a peer evaluation form in an Excel spreadsheet, and upload it to TCU Online. The instructor then downloads all the forms, runs an instructor-written Java program to parse and score them, finalizes grades, compiles comments, and uploads the results to TCU Online, where students can view their teammates' evaluations.

This process improves team efficiency but is overly manual and time-consuming. Students can make formatting mistakes (wrong columns, missing data) that cost them credit, and the instructor must perform many repetitive download / parse / upload steps by hand for the whole cohort.

**Requirements authoring.** Students currently author requirements using Google Docs templates, including:

- [Vision and Scope Document](https://docs.google.com/document/d/1h7Bho4auvUAE5zuPNh4Jkk0TBSNfom8P8F_e11kugqY/edit?usp=sharing)
- [Glossary](https://docs.google.com/document/d/1yqUkax6duHvEfFCP50IP16yoxQGyDaMHBT-CxUA73zk/edit?usp=sharing)
- [Use Cases](https://docs.google.com/document/d/1vaoprKQn58N4uE5gLaqLFLWtvsBYralnNWP_7rj4vJY/edit?usp=sharing)
- [Business Rules](https://docs.google.com/document/d/1vaoprKQn58N4uE5gLaqLFLWtvsBYralnNWP_7rj4vJY/edit?tab=t.0#bookmark=id.4b3zodpqhb2x)
- [Software Requirements Specification (SRS)](https://docs.google.com/document/d/1qXZTMvrdkSsjaGD9wQSs7aUFInp-8fyUd4IfaMNct0I/edit?usp=sharing)

While effective to a degree, this traditional process leads to incomplete document sections, inconsistent terminology across documents, low readability, weak traceability, and difficulty maintaining cross-document coherence.

To understand the full requirements of a project, students and instructors must manually open and cross-reference multiple documents. There is limited support for enforcing completeness, validating structure, or understanding how high-level goals map to concrete system behavior.

## **References**

- Wiegers, K., & Beatty, J. (2013). Software requirements. Pearson Education.
- [TCU COSC 40943/40993 Senior Design Templates](https://drive.google.com/drive/folders/1zhAg61cuGpplvg6zCTdvrY-9pTeH9Xg1?usp=sharing)

# **Business Requirements**

## **Business Opportunity/Problem Statement**

Project Pulse addresses two business problems, one for each capability area.

**Manual performance tracking.** The current weekly-activity-report and peer-evaluation process within the Computer Science Department is burdened by inefficiencies, errors, and delays. Students must download, complete, and upload spreadsheets on TCU Online, and faculty manage the evaluations by hand, which sometimes delays feedback. Automating these workflows — collecting weekly activity reports and peer evaluations in one place and compiling scores and feedback automatically — streamlines operations, improves data accuracy, and delivers timely feedback for both students and instructors across the department.

**Document-centric requirements authoring.** Modern IDEs provide powerful support for writing and maintaining code, including navigation, refactoring, validation, and dependency analysis. In contrast, requirements authoring in educational settings remains largely unchanged. Students continue to write requirements in generic, document-centric tools such as Microsoft Word or Google Docs—tools that are not designed to model the structure, relationships, or semantics of requirements engineering.

**Problems With Current Approach**

**1. Document-Centric, Not Model-Centric**

Current tools treat requirements as unstructured text rather than as structured, interrelated entities. Business objectives, features, use cases, functional requirements, and glossary terms exist only as prose, with no explicit representation of their relationships. As a result, understanding the system requires manually opening and cross-referencing multiple documents. This results in high cognitive load for understanding the Big Picture. To understand the requirements of a project, users must mentally integrate information spread across multiple documents. There is no unified view of the requirements as a system.

**2. No Enforcement of Completeness**

Students frequently leave required document sections blank or omit content they do not understand. Instructors cannot enforce that all necessary components—assumptions, constraints, glossary entries, metadata, risks—are fully completed.

**3. Unstructured, Essay-Like Free Text**

Generic document editors encourage narrative writing rather than atomic, specification-grade requirements. Because templates cannot be enforced:

- Students drift away from standard requirement structures
- Instructor-defined templates cannot be guaranteed
- Metadata and requirement identifiers are often missing or inconsistent

This directly undermines clarity, uniformity, and alignment with professional expectations.

**4. Terminology Drift Across Documents**

Without a central and shared project vocabulary, glossary terms, actors, system entities, and data objects diverge across documents. This leads to:

- inconsistent naming and definitions
- contradictions between artifacts
- breakdown of conceptual modeling

Terminology inconsistency remains one of the most common failure points in student requirements.

**5. Weak Validation and Writing Standards/Styles Enforcement**

Traditional word editors cannot perform requirement-specific checks, including:

- detecting ambiguity
- identifying vague verbs ("handle", "support", "process")
- flagging subjective adjectives ("fast", "user-friendly", "intuitive")
- verifying testability and measurable criteria
- ensuring required fields or formats are present
- enforcing company- or course-specific writing standards/styles. For example, a use case writing style rule might be:
  - _Use "The \<Primary Actor\> indicates to do . . . " or "The \<Primary Actor\> chooses to do . . . " to describe the trigger of the use case_.
    - _Positive example: The User indicates to search for products meeting defined search criteria._
    - _Positive example: The User chooses to search for products meeting defined search criteria._
- verifying naming conventions

Errors and noncompliant requirements persist undetected until late in the project.

**6. No Real-Time Quality Feedback**

The goal of a senior design or capstone project is for students to learn good industry practices. However, students receive no immediate explanation of why a requirement is poor. They do not learn:

- what makes a requirement ambiguous
- how to rewrite it to be testable
- how to use consistent terminology
- how to meet style/format standards

This leads to repeated mistakes and slow learning curves.

**7. Lack of End-to-End Traceability**

Current tools provide no support for linking:

- business requirements → product features
- product features → use cases
- use cases → functional requirements
- business rules → functional requirements
- glossary terms → all requirements
- constraints → design-impacting requirements

Missing traceability leads to:

- incomplete system coverage
- functional requirements with no rationale
- use cases missing supporting functional requirements
- inconsistent domain concepts
- poor change-impact analysis

This prevents students from learning traceability, a fundamental industry practice, and leads to incomplete or inconsistent specifications.

**8. Poor Integration With AI / LLM Tools**

Students often paste text into ChatGPT, but the integration is:

- unaware of project context
- unaware of templates or structure
- unable to enforce writing styles and rules
- disconnected from the project's requirement model

AI assistance becomes ad hoc rather than systematic or pedagogically aligned.

**9. Hard for Instructors to Grade Efficiently**

Manual review of long, inconsistent documents from multiple senior design teams is:

- slow and cognitively demanding
- prone to missing incomplete or low-quality requirements
- difficult to standardize across graders
- inefficient in large Senior Design cohorts

The lack of structure and automated checks directly increases instructor workload.

**Opportunity**

An education-focused, graph-first, model-driven requirements authoring environment can solve these persistent issues by:

- Modeling requirements as structured entities with explicit relationships
- Treating documents as views over a shared requirements model
- Enforcing structure so students produce clear, atomic, specification-grade requirements
- Enforcing customizable standards (IEEE, RUP, company/course-specific templates, domain-specific rules)
- Guiding students through prompts, examples, and AI-assisted coaching
- Ensuring terminology consistency across glossary, actors, data objects, and all requirement documents
- Providing real-time feedback on ambiguity, testability, completeness, structure, and writing style
- Enables end-to-end traceability across all requirement levels
- Supporting instructors with analytics, dashboards, completeness checks, and grading tools

The RAM tool will elevate student requirements to professional industry standards while modernizing and streamlining software engineering education.

## **Business Objectives**

BO-1: Improve the quality of student-written requirements through structure, templates, standards, traceability, AI feedback, and consistency checks.

BO-2: Reduce instructor workload by automating completeness, quality, and formatting validation.

BO-3: Ensure cross-document consistency for glossary terms, names, actors, and business rules.

BO-4: Support end-to-end traceability across business, user, and functional levels.

BO-5: Provide an integrated environment that supports learning outcomes in the Software Engineering and Senior Design class.

BO-6: Enable navigation, visualization, and impact analysis of requirements relationships. *(MVP delivers graph navigation and traceability linking; interactive visualization and change-impact analysis are post-MVP.)*

BO-7: Create opportunities for research in educational technology, AI-in-the-loop requirements engineering, NLP-based requirement quality analysis, and HCI.

BO-8: Reduce the instructor's time to grade peer evaluations by 50%.

BO-9: Increase students' weekly activity report and peer evaluation submission rate by 20%.

BO-10: Reduce the time students spend completing their weekly activity reports and peer evaluations by 25%.

## **Vision Statement**

Project Pulse is the platform for the senior design course. On the performance-tracking side it replaces the manual spreadsheet workflow for weekly activity reports and peer evaluations with an integrated system that collects submissions and automatically compiles scores and feedback. On the requirements side it houses RAM, a modern, web-based requirements engineering IDE that captures requirements as a structured, navigable graph rather than isolated documents: every problem, objective, feature, use case, functional requirement, risk, and glossary term is modeled as a first-class entity with explicit relationships, and document templates (Vision and Scope, Glossary, Use Cases, Business Rules, SRS) are views over the shared requirements model, enabling trace navigation, impact analysis, and IDE-like refactoring (e.g., renaming a glossary term updates all references). Together, Project Pulse empowers students to track their contribution and to understand, author, and evolve requirements holistically, while giving instructors powerful tools for evaluation grading and for requirement validation, feedback, and grading.

|                    |                                                                                                                                                                                                                                                                                               |
| :----------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| For                | university students and instructors in software engineering, senior design, or capstone courses, |
| Who                | need to track team contribution and to author, validate, and manage software requirements in one place, |
| The (product name) | Project Pulse |
| That               | streamlines weekly activity reports and peer evaluations and provides a graph-first, model-driven environment for authoring requirements — enforcing instructor-defined templates, maintaining terminology consistency, supporting full traceability, and delivering real-time quality feedback, |
| Unlike             | the manual spreadsheet-and-LMS process for performance tracking and the generic word processors used for requirements, neither of which enforces structure, automation, standards, or requirements-specific validation, |
| Our product        | brings both into a single web platform — automating evaluation collection, scoring, and reporting, and delivering a customizable, education-focused requirements IDE with template enforcement, writing standards, terminology consistency checks, and LLM-powered guidance — to reduce instructor workload and raise student work to professional industry standards. |

## **Proposed New/Improved Process Flows (To-Be Process Flows)**

**Weekly activity reports and peer evaluations.** Both workflows move entirely into Project Pulse. For weekly activity reports, students record their activities and instructors review and grade them within the system, eliminating the shared spreadsheets and reducing manual steps and errors; one manual task remains — the instructor still uploads final grades to the university LMS, which is outside the scope of this project. For peer evaluations, students review their team's WAR for the week and evaluate their teammates in Project Pulse; once evaluations are in, the system compiles them for the whole course section, calculates scores, and generates feedback. The instructor reviews the aggregated scores and feedback directly in Project Pulse, and each student can independently view her own scores and feedback. As with WARs, the only remaining manual step is the instructor uploading grades to the LMS.

**Requirements authoring.** The to-be process describes how a student team authors a project's requirements in RAM, from the client's initial pitch through a delivered specification. RAM's AI assistants are deliberately Socratic: they coach the students' own elicitation and writing rather than produce finished requirements.

1. **Course setup (course admin and instructor).** The instructor authors the teaching context — the standards, common student mistakes, and thinking order the assistants follow — and the per-assistant assistant instructions that shape each assistant's behavior, and enables the appropriate AI assistants for the course section; the course admin has each team's requirement documents instantiated from the built-in templates.
2. **Project seed.** The client pitches the project to the team with materials such as a slide deck and a short brief covering background, stakeholders, problem statement, users, objectives, desired functionality, possible solutions, prototypes, and a candidate tech stack. The team imports these into RAM as project source material so that the team and the AI assistants can reference them.
3. **Elicitation preparation.** Before meeting the client, the team works with the elicitation assistant, which performs a gap analysis of the pitch against what complete Vision and Scope, Use Cases, Business Rules, and SRS documents require, and helps the students turn the gaps into a prioritized list of interview questions phrased in plain, non-technical language. The team can rehearse the conversation against a role-playing client assistant.
4. **Client elicitation.** The team interviews the client, or runs a requirements workshop, off-platform using their question list, and captures notes and answers.
5. **Verification and structuring.** Back in RAM, the team first uses the elicitation assistant to verify the client's answers — surfacing vague, contradictory, or missing information that drives follow-up questions and additional client conversations (returning to steps 3–4 as needed). Once the answers are verified, the team uses the structuring assistant to translate its meeting notes into candidate structured requirements, accepting or rejecting each.
6. **Structured authoring.** Guided by the templates, the team drafts the documents in their natural progression — Vision and Scope, then Use Cases, then the SRS — while the Glossary and Business Rules documents grow continuously as terms and rules surface. When working on a particular document section, the student uses "help me elicit" to focus the elicitation assistant on that document section and requests a critique assistant review for clarity, ambiguity, consistency, completeness, and testability, each finding accompanied by an instructive rationale; a tutor assistant explains concepts on demand. Smart editing assists with glossary-term suggestions; broader auto-suggestions (actors, requirement patterns, cross-document links) and auto-complete for requirement types and scenarios are planned post-MVP.
7. **Linking and traceability.** As artifacts accumulate, the team makes the requirements graph explicit by creating typed requirement links between requirement artifacts — for example, a student links a use case to the functional requirements and quality attributes that realize it, or traces a functional requirement upstream to the business objective it serves — choosing the appropriate link type and consulting the Glossary when unsure what a given link type or traceability relationship means. The team then uses graph navigation to follow these links upstream and downstream, confirming each requirement traces to a source and surfacing orphans (artifacts with no incoming or outgoing links) to resolve.
8. **Continuous validation.** Automated "ReqLint" validation runs continuously — completeness checks, vagueness detection ("adequate", "fast", "user-friendly"), glossary conflicts, naming violations, and format and writing-style compliance — and the team resolves the flagged issues, using the tutor assistant to understand any rule.
9. **Review and grading.** Students submit documents for review; instructors use dashboards for progress tracking, completeness percentages, quality scores, and rubric-based grading assistance, and return documents for revision as needed.
10. **Delivery.** The team exports the polished documents, individually or as a bundle, to hand off to the client and to drive downstream code generation.

The process is iterative: as authoring reveals gaps, the team returns to elicitation (steps 3–5) for further client conversations before finalizing and delivering.

## **Risks**

Risks to the success of the system or project — spanning business/adoption, technical/feasibility, and security/safety categories. Each is identified by an `RI-*` key.

RI-1: Adoption Resistance: Students comfortable with Microsoft Word or Google Docs may resist structured tools.

RI-2: LLM reliability: AI suggestions may occasionally conflict with instructor expectations.

RI-3: Scalability concerns: High usage in large capstone programs may stress infrastructure.

RI-4: Over-reliance on AI: Students might depend too heavily on AI-generated content.

RI-5: Browser compatibility: Ensuring smooth performance across many devices.

RI-6: Accessibility: Must meet WCAG accessibility requirements.

RI-7: Cloud cost ownership: if the system is deployed on a cloud service provider, the Computer Science Department must budget for the annual cloud fees.

RI-8: Data breach: the students' peer evaluation data may be targeted by attackers (addressed by the security quality attributes in the Software Requirements Specification).

RI-9: Usability regression: the application could prove more confusing to use than the previous manual process (a specific instance of the adoption resistance in RI-1).

RI-10: Narrow applicability: the application might prove too specialized for the TCU senior design course to be reused in other courses.

## **Business Assumptions and Dependencies**

AS-1: Students and instructors have stable internet access.

AS-2: Courses already require formal documentation.

AS-3: Microsoft Azure infrastructure is available and supported.

AS-4: LLM APIs (e.g., OpenAI) remain available and cost-effective.

AS-5: Faculty buy-in will be sufficient for pilot use.

AS-9: The system uses technologies the client already knows and can maintain after the product is delivered. *(AS-6 through AS-8 are architecture-level assumptions defined in the Software Requirements Specification; the `AS-*` sequence is shared across both documents.)*

# **Stakeholder Profiles and User Descriptions**

## **Stakeholder Profiles**

| Stakeholder                                                      | Major value or benefit from this product                                                                                                                               | Attitudes                                                                                                              | Major features of interest                                                                                                          | Constraints                                                                     | End user or not? |
| ---------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- | ---------------- |
| Undergraduate CS Students (Software Engineering & Senior Design) | Clear guidance on writing high-quality requirements; reduced confusion; structured templates; AI feedback; improved grades; fewer errors; professional-grade documents; one place to submit weekly activity reports and peer evaluations without uploading or downloading files | Generally willing to use new tools if easy-to-learn; expect modern UI; may resist if overly rigid; variable motivation | Smart editors, LLM assistance, auto-completeness checking, glossary linking, use case templates, version history, weekly activity report and peer evaluation submission                    | Limited experience with requirements engineering; time pressure; learning curve | Yes              |
| Software Engineering & Senior Design Instructors                 | Teach requirements effectively; efficient grading; consistent formatting; Ability to enforce standards; dashboards for progress; reduced manual review; faster peer-evaluation grading and clearer insight into team dynamics                 | Strongly supportive if tool reduces grading burden; expect accuracy and reliability                                    | Template customization, LLM tutoring, example patterns, traceability, dashboards, completeness metrics, requirement quality scoring, WAR review, peer-evaluation report generation | Limited time; large class sizes; need reliable, accurate checks                 | Yes              |
| Course Admin (course creator)                                    | Efficient course section and team setup; reliable roster and access management; one-click provisioning of each team's documents from built-in templates                | Wants minimal-friction setup; values reliability, correctness, and auditability                                        | Course section / team creation, roster and enrollment management, instructor invitations, template assignment, document provisioning | Owns a single course and is also an Instructor of it; limited time; needs correctness and an audit trail | Yes              |

## **User Environment**

- Web-based interface accessible via Chrome, Safari, and Firefox.
- Accessible from a desktop, laptop, or mobile device, regardless of operating system.
- Cloud-hosted on Microsoft Azure.
- Students will work individually or collaboratively.
- Instructors typically use desktop or laptop environments for grading.
- Tool must support high concurrency during assignment deadlines.

## **Alternatives and Competition**

| Tool                         | Strengths                       | Weakness for Academic use                               |
| :--------------------------- | :------------------------------ | :------------------------------------------------------ |
| IBM DOORS                    | Industry standard, powerful     | Overkill for education; steep learning curve; expensive |
| Perforce/Helix ALM           | Full ALM suite                  | Enterprise-oriented; not student-friendly               |
| reqSuite                     | Simpler requirements management | Limited integration with teaching workflows             |
| Microsoft Word / Google Docs | Familiar, flexible              | No structure, no validation, no consistency checking    |
| Manual spreadsheets + TCU Online (weekly activity reports / peer evaluations) | Familiar; no new tool to learn | Manual, error-prone, and time-consuming for students and instructors; no automation of scoring or reporting |

# **Scope and Limitations**

## **Product Perspective**

**Project Pulse** is a web application for the senior design course, deployed as a single platform (a Vue.js SPA served by a Spring Boot REST API over a shared relational database). It provides course, course section, team, and user management, authentication, and email notifications, and on that foundation delivers two capability areas: the weekly activity report and peer evaluation workflows that track student performance, and the Requirements Authoring & Management (RAM) environment — structured templates, validation, AI-assistant assistance, collaboration, and instructor evaluation tools — for authoring software requirements.

## **Major Features / Scope**

Project Pulse provides a modern environment for running the senior design course — tracking student performance through weekly activity reports and peer evaluations, and authoring, validating, and managing software requirements. The features below describe the major functional capabilities at a high level. Detailed behavioral descriptions, actor interactions, and exception scenarios are defined in the accompanying [Use Cases document](use-cases.md). The requirements-authoring features come first, followed by the performance-tracking and course-management features.

### *Graph-First Requirements Model*

The tool is not a document editor intended to replicate Google Docs or Microsoft Word. Instead, RAM is model-first: it stores requirements as a connected domain model (a requirements graph) where nodes represent requirement elements (e.g., objectives, features, use case steps, functional requirements, glossary terms) and edges represent relationships (traceability, realization, references). Traditional "documents" are generated and edited as structured views of this underlying model. This enables capabilities that are difficult or impossible in text-first tools, including instant navigation across requirement chains (objective → feature → use case step/extension → functional requirement), "find all references" for glossary terms, safe rename, and change-impact analysis. *(MVP scope: the requirements graph with trace navigation and safe rename ships in the initial release; interactive graph visualization and change-impact analysis are post-MVP — see [Full Requirements Traceability](#full-requirements-traceability).)*

### *Template Management*

The tool includes built-in structured document templates (based on Wiegers & Beatty's Software Requirements 3rd Edition Book) for Vision and Scope, Use Cases, Glossary, Business Rules, SRS, and other requirement documents. These templates define the structure of each document and give students clear expectations for required document sections, content, and writing styles and standards.

Course admins or instructors can also create new structured document templates based on industry standards or course needs, including:

- Document section structure and ordering
- Required vs. optional fields
- Examples and instructional prompts
- Writing styles, terminology rules, and validation settings
- Grading rubrics tied to specific document sections

This enables flexibility across methodologies and supports instructor-specific teaching goals. Template customization by a course admin or instructor is a post-MVP capability; the initial release ships with fixed, built-in templates.

### *Smart Editing and Validation (ReqLint)*

The tool includes ReqLint, a lightweight rule-based validation engine inspired by code linters such as ESLint. ReqLint is non-AI and performs deterministic checks that enforce structure, completeness, and writing standards. Typical checks include:

- Incorrect formatting (e.g., missing "shall" statements in functional requirements, missing the actor in use case steps)
- Usage of vague verbs (e.g., "manage", "support") or subjective adjectives (e.g., "quick", "user-friendly")
- Ambiguous or non-testable requirement shapes
- Glossary and actor name consistency (e.g., usage of terminology not defined in the Glossary)
- Identifier and cross-reference correctness

A central and shared glossary is integrated into ReqLint to maintain consistent terminology for actors and domain concepts across all documents. ReqLint ensures students meet template and writing standards before any AI guidance is applied.

Beyond glossary-term suggestion, which the initial release provides, broader smart-editing assistance — auto-suggesting actors, requirement patterns, and cross-document links, and auto-completing requirement types and scenarios — is **post-MVP**; the MVP focuses on deterministic ReqLint validation together with glossary-term suggestion.

### *Full Requirements Traceability*

The tool will support bidirectional traceability across all major requirement types, enabling students and instructors to see how business goals flow down into user requirements and functional specifications. Users can link:

- Business Requirements → Product Features
- Product Features → Use Cases (User Requirements)
- Use Cases → Functional Requirements
- Functional Requirements → Constraints, Data Requirements, Quality Attributes
- Business Rules → Use Cases / Functional Requirements
- Glossary Terms → All Requirements

Interactive traceability-matrix generation, highlighting of missing or incomplete linkages and orphan artifacts, and basic change-impact analysis when upstream or downstream requirements are modified are **post-MVP** capabilities; the initial release supports creating and navigating the typed links themselves (see the Use Cases document, *Artifact Links and Tracing*: UC-LNK-1 through UC-LNK-6). This allows students to practice industry-standard traceability and helps instructors quickly evaluate completeness, coverage, and consistency across all requirement artifacts.

### *AI-Assisted Guidance and Feedback*

Beyond rule-based validation, RAM provides AI assistance through a set of deliberately Socratic assistants whose purpose is educational: they coach students to author high-quality requirements and to communicate with non-technical clients, rather than producing finished requirements. Each assistant behaves according to its instructor-authored assistant instructions — the per-assistant role, persona, and boundaries — and draws on the course section's teaching context (the standards, common student mistakes, and thinking order the course teaches); instructors enable or disable each assistant per course section.

The assistants include:

- **Elicitation assistant** — coaches the student's own requirements elicitation: it performs a gap analysis — project-wide across the team's documents and sections for early-phase workshop prep, or focused on a target document section or use case against its template — helps the student plan client-interview questions in plain, non-technical language, suggests follow-ups, and helps verify the client's answers. It grounds on the imported project source material, which the student can exclude for a session later in the project once the pitch has gone stale. It questions; the student writes.
- **Critique assistant** — reviews an authored document section or use case for clarity, ambiguity, consistency, completeness, and testability, attaching an instructive rationale to each finding (distinct from, and complementary to, deterministic ReqLint).
- **Tutor assistant** — explains a concept, glossary term, or flagged issue on demand; for a flagged issue, it identifies the rule involved and a suggested fix.
- **Client role-play assistant** — simulates a vague, non-technical client so students can rehearse interviews and practice extracting requirements off-platform.
- **Structuring assistant** — helps a student translate her own client-meeting notes into candidate structured requirements, each traceable to a source note and accepted or rejected individually; it structures the student's elicited input rather than generating content from a prompt.
- **Drafting assistant (optional)** — when an instructor enables it, proposes structural skeletons or candidate requirements from a short prompt. It is disabled by default in course use and is intended chiefly for experienced/real-developer use.
- **Project assistant** — a project-level conversational front door in the project workspace: it orients the student, answers questions about requirements status and coverage, helps her navigate to the right document or artifact, and routes her to the specialized assistant for the task (for example, launching broad, project-wide elicitation through the elicitation assistant). It coordinates the other assistants by routing the student to the right one and coaching her next step; it routes and explains rather than authoring requirements.

Two principles govern every assistant:

- **Authorship stays with the student.** AI never overwrites student content automatically. Any concrete proposal (a rewrite, skeleton, or candidate requirement) is applied only through an explicit propose → inspect → accept loop, with no "accept all" shortcut — the friction is intentional and pedagogical.
- **Rationale is the lesson.** Every finding or proposal carries an instructive explanation, so students learn why, not just what.

Combined with ReqLint, the assistant layer improves learning outcomes, trains client communication, and reduces instructor workload while preserving student authorship.

### *Collaboration and Document Workflow*

Student teams can collaborate in real time through presence indicators, document-section-level editing, and commenting. The tool supports submission-and-review workflows that allow students to submit drafts, receive instructor feedback, and revise their work iteratively.

Document version history — checkpointing each save, viewing prior versions, and restoring them — is a **post-MVP** capability; the initial release keeps only authorship metadata (the creator/editor and timestamps recorded on every authored item).

### *Instructor Dashboard, Feedback, and Grading*

Instructors will have access to dashboards summarizing team progress, document completeness, and validation results. Integrated grading tools enable rubric-based evaluation aligned with the selected templates, as well as inline comments and structured feedback. This improves grading consistency and reduces review effort.

**MVP scope:** the initial release provides only the review-and-feedback workflow — an instructor opens a submitted document, leaves inline comments, and either accepts it or returns it for revision (see the Use Cases document, *Review and Submission*: UC-REV-1, UC-REV-2, and commenting UC-COL-2 / UC-COL-3, governed by the business rules in [business-rules.md](business-rules.md)). Progress/completeness dashboards and rubric-based grading are **post-MVP**.

### *Project Source Material Import*

At the start of a project the client provides pitch materials — typically a slide deck and a short brief — describing the background, stakeholders, problem, users, objectives, desired functionality, possible solutions, prototypes, and a candidate tech stack. Students import these into RAM as project source material, the input the team works from. RAM stores the materials, extracts their text, and makes them available as context to the AI assistants — most directly to the elicitation assistant, which uses them in its gap analysis (project-wide across the team's documents and sections for early-phase workshop prep, or focused on a target against its template) to help the team prepare client-interview questions. Because the pitch goes stale as the project evolves, later-authored, verified requirements take precedence over it, and a student can exclude the project source material from an elicitation session entirely. Project source material is a project input, not authored requirement content, and is not itself graded.

### *Export and Delivery*

Students can export requirement documents to professional-quality PDF, Word (DOCX), or Markdown formats that follow the structure of the chosen template (Markdown prioritizes structure and traceability over visual styling, and supports downstream code generation). Exported documents are suitable for client or stakeholder review in senior design and capstone projects.

### *Weekly Activity Reports*

Each week, students record their work as a weekly activity report — for each activity, its category, the planned activity, a description, hours planned, actual hours taken, and status. Teammates and instructors can review a team's reports, and the system generates a WAR report for a team or for an individual student. This replaces the shared-spreadsheet workflow and fosters communication within the team. (See the Use Cases document, *Weekly Activity Report*: UC-WAR-1 through UC-WAR-3.)

### *Peer Evaluations*

During active weeks, students evaluate their teammates against a rubric of weighted criteria, optionally adding public comments (shared with the evaluatee) and private comments (visible only to the instructor). The system aggregates scores and comments, generates a peer-evaluation report for the entire course section and per-student reports, and lets each student view her own results. Submission windows, edit rules, and visibility are governed by the business rules in [business-rules.md](business-rules.md) (BR-22 through BR-25). (See the Use Cases document, *Peer Evaluation*: UC-EVA-1 through UC-EVA-4.)

### *Administration and Course Management*

The system supports secure authentication, role-based access control, and FERPA-compliant data storage. It provides course, course section, team, student, and instructor management for the whole platform: the course admin (the user who created the Course) sets up its course sections and project teams, manages rosters and document access, invites instructors, configures active weeks, and provisions each team's requirement documents from the built-in templates; instructors configure their course sections' teaching context and AI assistants and run the review-and-feedback workflow. The course admin (the user who created the Course) sets up its course sections and project teams, manages rosters and document access, invites instructors, and provisions each team's requirement documents from the built-in templates; instructors configure their course sections' teaching context and AI assistants and run the review-and-feedback workflow.

**See the Use Cases document for more details.**

MVP Scope (Initial Classroom Deployment)

The course-operations and performance-tracking capabilities — course / course section / team / roster management, weekly activity reports, and peer evaluations — are already established and in production. The scope statement below concerns the newer Requirements Authoring & Management (RAM) capability. The initial release of RAM will focus on structured, collaborative authoring of requirements documents (Vision and Scope, Glossary, Use Cases, Business Rules, SRS) using fixed, built-in templates, together with the Socratic AI assistants that coach requirements authoring and client elicitation (the elicitation, critique, tutor, structuring, and client role-play assistants), reached through the project assistant — the project-level conversational front door that orients students and routes them to the right assistant. The MVP will exclude free-form AI generation of finished requirements (the drafting assistant is off by default in course use), template customization, advanced analytics and instructor dashboards, automated/rubric grading, document version history (checkpointing each save, viewing prior versions, and restoring them), the interactive traceability-matrix view, graph visualization, and change-impact analysis (the typed links and trace navigation themselves remain in scope), and broader smart-editing auto-suggestions and auto-complete beyond glossary-term suggestion — these capabilities are deferred to a future release — focusing instead on correctness, traceability, collaboration, coaching, and export quality. In the MVP, instructor support is limited to the review-and-feedback workflow (see [Instructor Dashboard, Feedback, and Grading](#instructor-dashboard-feedback-and-grading)).

## **Deployment Considerations**

- Deploy to Microsoft Azure App Service
- OpenAI API for LLM integration
- CI/CD using GitHub Actions
- Load testing for peak usage
