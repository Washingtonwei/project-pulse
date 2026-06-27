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

- **Student Performance Tracking** — when students work together on a team project, individual effort is easy to hide and hard to assess fairly; this area keeps each student's contribution visible by replacing the manual, spreadsheet-based collection of weekly activity reports (WARs) and peer evaluations with an integrated, automated workflow.
- **Requirements Authoring & Management (RAM)** — students rarely arrive able to write clear, professional requirements, yet weak requirements quietly derail projects and increasingly misdirect AI-assisted coding; RAM is a software requirements authoring environment that modernizes how software requirements are written, understood, and evaluated, replacing old-school, document-centric requirement writing with a structured, model-driven approach that reflects professional requirements engineering practices while remaining accessible to students and instructors. Requirements become a graph-first model of atomic, interlinked artifacts — unlocking end-to-end traceability, real-time quality validation, and Socratic AI assistants that coach students rather than write for them.

This document owns the project's motivation identifiers — business objectives (`BO-<AREA>-<slug>`, area-prefixed by capability: `BO-PERF-*` for performance tracking, `BO-RAM-*` for requirements authoring) and their success metrics (`SM-<slug>`), major features (`FEAT-<slug>`, a flat name-based slug — a feature spans several use-case areas, so it carries no area segment), risks (`RI-<slug>`), and assumptions (`AS-<slug>`) — alongside the stakeholders. The downstream docs cite these by identifier rather than restate them: use cases (`UC-<AREA>-<slug>` in [use-cases.md](use-cases.md)) and non-use-case functional requirements (`FR-<AREA>-<slug>` in [software-requirements-specification.md](software-requirements-specification.md)) link to the business objective or feature they realize, the [traceability matrix](../traceability.md) carries the spec→code map keyed to those IDs, and cross-cutting policies are catalogued in [business-rules.md](business-rules.md) (`BR-<slug>`). Defined terms used here are owned by [project-glossary.md](project-glossary.md).

## **Background**

The Department of Computer Science at TCU teaches a two-semester Software Engineering / Senior Design sequence — the undergraduate capstone, in which students consolidate what they have learned by building a real system for a real client. Each team is paired with an external client who brings a genuine problem and a project idea, and over the academic year the team carries that project through the full lifecycle: eliciting requirements from the client, writing and validating a specification, designing the system, implementing it, and delivering a working product. For most students this is their first experience of professional software engineering end to end — working with a non-technical stakeholder whose needs are initially vague, and depending on teammates while remaining individually accountable for their own contribution. Two needs run through the whole experience: tracking each student's weekly contribution so that individual effort stays visible within shared team work, and producing professional-grade requirements the team and the client can rely on. Project Pulse is built to support both.

**Team contribution and accountability.** Teamwork and individual accountability are themselves learning outcomes of the course, not merely administrative concerns. When several students share responsibility for one system, individual effort naturally varies, and uneven contribution is easy to hide and hard to assess fairly. To keep contribution visible, the course has long relied on two complementary practices. In a weekly activity report, each student records what they worked on that week — the activities planned and completed, the hours spent, and the status of each — producing a running, individual record of effort. In a peer evaluation, teammates periodically rate one another against a rubric of weighted criteria and add comments, surfacing how the team actually functions from the inside. Together these practices give the instructor the evidence to grade each student's individual contribution fairly and, just as importantly, cultivate the communication, self-awareness, and honest self- and peer-appraisal that professional engineers are expected to practice throughout their careers.

**Requirements engineering.** Requirements are the foundation of all software development, describing what a system must do, how it behaves, and the constraints under which it must operate; a project built on unclear or incomplete requirements rarely recovers later in the lifecycle. Eliciting requirements from a real, often non-technical client and writing them clearly, consistently, and professionally is therefore a core competency the senior design course must teach — and one most students have never practiced before. The stakes are only rising. As large language models take on more of the coding, the quality of the requirements increasingly determines the quality of the result: clear, unambiguous requirements yield better AI-assisted implementations, while poorly specified ones lead to misinterpretation, unintended behavior, and incorrect code. Teaching students to write good requirements is thus both a long-standing discipline and an increasingly decisive skill — a worthwhile investment for the course and the department.

## **Current Process Flows (As-Is Process Flows)**

**Weekly activity reports and peer evaluations.** A student-performance tracking process is already in place, run manually with spreadsheets:

- *Weekly activity report (WAR).* Every student completes a weekly activity report each week recording what they did. WARs are kept in shared Google Sheets — one sheet per team per week — that teammates can edit and view, which also fosters communication. Each Monday students record the previous week's activities in their team's sheet; the instructor keeps the URLs of all the sheets, reviews the updated ones on Tuesday, and grades and gives feedback through the university LMS (TCU Online).
- *Peer evaluation.* Each Tuesday students first review the team's WAR from the previous week, complete a peer evaluation form in an Excel spreadsheet, and upload it to TCU Online. The instructor then downloads all the forms, runs an instructor-written Java program to parse and score them, finalizes grades, compiles comments, and uploads the results to TCU Online, where students can view their teammates' evaluations.

This process improves team efficiency but is overly manual and time-consuming. The submission workflow spans two systems and several discrete steps — open the team's Google Sheet on Monday to record last week's activities; on Tuesday, download the peer-evaluation Excel template, fill it in, and upload it to TCU Online — and under deadline pressure students often forget one of the steps, or miss a submission entirely, costing them credit. Students can also make formatting mistakes (wrong columns, missing data) that cost them credit, and the instructor must perform many repetitive download / parse / upload steps by hand for the whole cohort.

**Requirements authoring.** Students currently author requirements using Google Docs templates, including:

- [Vision and Scope Document](https://docs.google.com/document/d/1h7Bho4auvUAE5zuPNh4Jkk0TBSNfom8P8F_e11kugqY/edit?usp=sharing)
- [Glossary](https://docs.google.com/document/d/1yqUkax6duHvEfFCP50IP16yoxQGyDaMHBT-CxUA73zk/edit?usp=sharing)
- [Use Cases](https://docs.google.com/document/d/1vaoprKQn58N4uE5gLaqLFLWtvsBYralnNWP_7rj4vJY/edit?usp=sharing)
- [Business Rules](https://docs.google.com/document/d/1vaoprKQn58N4uE5gLaqLFLWtvsBYralnNWP_7rj4vJY/edit?tab=t.0#bookmark=id.4b3zodpqhb2x)
- [Software Requirements Specification (SRS)](https://docs.google.com/document/d/1qXZTMvrdkSsjaGD9wQSs7aUFInp-8fyUd4IfaMNct0I/edit?usp=sharing)

While workable, this traditional process leads to incomplete document sections, inconsistent terminology, weak traceability, and poor cross-document coherence: to understand a project's full requirements, students and instructors must manually open and cross-reference multiple documents, with little support for enforcing completeness or validating structure.

## **References**

- Wiegers, K., & Beatty, J. (2013). Software requirements. Pearson Education.
- [TCU COSC 40943/40993 Senior Design Templates](https://drive.google.com/drive/folders/1zhAg61cuGpplvg6zCTdvrY-9pTeH9Xg1?usp=sharing)

# **Business Requirements**

## **Business Opportunity/Problem Statement**

Project Pulse addresses two business problems, one for each capability area.

**Manual performance tracking.** The current weekly-activity-report and peer-evaluation process within the Computer Science Department is burdened by inefficiencies, errors, missed submissions, and delays. Students must track due dates across two systems and download, complete, and upload spreadsheets on TCU Online — a multi-step routine that students under deadline pressure often forget, costing them credit — and faculty manage the evaluations by hand, which sometimes delays feedback. Automating these workflows — collecting weekly activity reports and peer evaluations in one place and compiling scores and feedback automatically — streamlines operations, improves data accuracy, raises submission rates, and delivers timely feedback for both students and instructors across the department.

**Document-centric requirements authoring.** Modern IDEs provide powerful support for writing and maintaining code, including navigation, refactoring, validation, and dependency analysis. In contrast, requirements authoring in educational settings remains largely unchanged. Students continue to write requirements in generic, document-centric tools such as Microsoft Word or Google Docs—tools that are not designed to model the structure, relationships, or semantics of requirements engineering.

**Problems With Current Approach**

**1. Document-Centric, Not Model-Centric**

Current tools treat requirements as unstructured text rather than as structured, interrelated entities, with no explicit representation of how business objectives, features, use cases, functional requirements, and glossary terms relate. Understanding a project means manually opening and cross-referencing multiple documents and mentally integrating them — there is no unified view of the requirements as a system, and the cognitive load of seeing the big picture is high.

**2. No Enforcement of Structure or Completeness**

Generic editors cannot enforce a template, so students drift from standard requirement structures, leave required document sections (assumptions, constraints, glossary entries, metadata, risks) blank, and write narrative prose rather than atomic, specification-grade requirements. Requirement identifiers and metadata are often missing or inconsistent. This undermines clarity, uniformity, and alignment with professional expectations.

**3. Terminology Drift Across Documents**

Without a central, shared project vocabulary, glossary terms, actors, system entities, and data objects diverge across documents, producing inconsistent naming, contradictions between artifacts, and a breakdown of conceptual modeling. Terminology inconsistency remains one of the most common failure points in student requirements.

**4. Weak Validation and No Real-Time Quality Feedback**

Traditional editors cannot perform requirement-specific checks — detecting ambiguity, vague verbs ("handle", "support", "process"), or subjective adjectives ("fast", "user-friendly"); verifying testability and measurable criteria; ensuring required fields are present; or enforcing course-specific writing standards and naming conventions. Noncompliant requirements therefore persist undetected, and because the tool offers no immediate explanation of *why* a requirement is poor or how to rewrite it, students repeat the same mistakes and learn slowly.

**5. Lack of End-to-End Traceability**

Current tools provide no support for linking requirements across levels — business requirements → features → use cases → functional requirements, business rules and glossary terms → the requirements they govern, constraints → design-impacting requirements. The result is incomplete system coverage, functional requirements with no rationale, use cases missing supporting requirements, and poor change-impact analysis — and students never learn traceability, a fundamental industry practice.

**6. Poor Integration With AI / LLM Tools**

Students often paste text into ChatGPT, but the integration is unaware of project context, templates, and structure, and disconnected from the project's requirement model, so it cannot enforce writing styles and rules. AI assistance becomes ad hoc rather than systematic or pedagogically aligned.

**7. Hard for Instructors to Grade Efficiently**

Manually reviewing long, inconsistent documents from many senior design teams is slow and cognitively demanding, prone to missing incomplete or low-quality requirements, hard to standardize across graders, and inefficient in large cohorts. The lack of structure and automated checks directly increases instructor workload.

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

Each objective carries an area-prefixed `BO-<AREA>-<slug>` identifier — parallel to the `UC-<AREA>-<slug>` and `FR-<AREA>-<slug>` schemes — grouping it by the capability area it serves: `PERF` (performance tracking) or `RAM` (requirements authoring). As with those schemes, the `<slug>` is a name-based handle, unique within its area and independent of reading position, so a new objective is added to its area without disturbing the others.

Each objective's achievement is made measurable by a **success metric** (`SM-<slug>`, a flat name-based slug) defined immediately beneath it — a baseline, a target, a measurement method, and an evaluation cadence. The success metric is the **outcome → objective backward edge** (the means of telling whether the objective was actually met, not merely built); where an objective is genuinely qualitative, its metric says so and names the empirical method (survey, rubric scoring, instructor judgment) rather than inventing a false number. The [BO measurement matrix](../traceability.md#bo-measurement-matrix) in traceability.md records each metric's evaluation route and state. A purely platform-level emergent objective carries no success metric (the `*(Platform-level emergent objective; …)*` carve-out); `/spec-build` flags any other BO with no `SM-<slug>`.

**Performance tracking.**

BO-PERF-faster-grading: Reduce the instructor's time to grade peer evaluations by 50%.

- SM-grading-time: Baseline — instructor-reported hours to grade one course section's peer evaluations under the spreadsheet process; target — 50% reduction; method — instructor time self-report, pre- vs post-adoption; evaluated at the end of the first piloted term.

BO-PERF-instructor-workload: Reduce instructor workload by automating the end-to-end processing of weekly activity reports and peer evaluations — collection, parsing, scoring, comment compilation, and result distribution — eliminating the manual download / parse / upload cycle the instructor runs each week for the whole cohort.

- SM-perf-manual-steps: Baseline — number of manual download / parse / upload steps the instructor performs per week under the spreadsheet process; target — eliminate the recurring manual cycle (qualitative); method — workflow step-count plus instructor self-report; evaluated per term.

BO-PERF-submission-rate: Increase students' weekly activity report and peer evaluation submission rate by 20%.

- SM-submission-rate: Baseline — on-time weekly activity report and peer evaluation submission rate from the spreadsheet era (LMS records); target — 20% increase; method — system submission logs compared against the historical baseline; evaluated per term.

BO-PERF-faster-completion: Reduce the time students spend completing their weekly activity reports and peer evaluations by 25%.

- SM-completion-time: Baseline — student-reported minutes per weekly activity report and peer evaluation; target — 25% reduction; method — student time survey; evaluated per term.

**Requirements authoring (RAM).**

BO-RAM-requirement-quality: Improve the quality of student-written requirements through structure, templates, standards, traceability, AI feedback, and consistency checks.

- SM-requirement-quality: Baseline — rubric-scored quality of prior cohorts' requirement documents; target — measurable improvement (qualitative); method — instructor rubric scoring against prior cohorts; evaluated per term.

BO-RAM-instructor-workload: Reduce instructor workload by automating completeness, quality, and formatting validation.

- SM-ram-grading-workload: Baseline — instructor-reported effort to check completeness, quality, and formatting of student requirements; target — measurable reduction (qualitative); method — instructor self-report; evaluated per term.

BO-RAM-consistency: Ensure cross-document consistency for glossary terms, names, actors, and business rules.

- SM-terminology-consistency: Baseline — terminology / naming inconsistency count across a team's documents; target — measurable reduction (qualitative); method — ReqLint terminology-violation counts once built, manual review until then; evaluated per term.

BO-RAM-traceability: Support end-to-end traceability across business, user, and functional levels.

- SM-traceability-completeness: Baseline — orphan-artifact and missing-link counts in prior cohorts' projects; target — measurable reduction (qualitative); method — requirements-graph metrics on student projects; evaluated per term.

BO-RAM-learning-outcomes: Provide an integrated environment that supports learning outcomes in the Software Engineering and Senior Design class.

- SM-learning-outcomes: Baseline — the course's requirements-engineering learning-outcome assessment for prior cohorts; target — measurable improvement (qualitative); method — course learning-outcome assessment / instructor evaluation; evaluated per term.

BO-RAM-navigation: Enable navigation, visualization, and impact analysis of requirements relationships. *(MVP delivers graph navigation and traceability linking; interactive visualization and change-impact analysis are post-MVP.)*

- SM-navigation-usage: Baseline — none (new capability); target — students use trace navigation to find and resolve orphan artifacts (qualitative); method — feature-usage observation plus instructor and student feedback; evaluated per term.

BO-RAM-research: Create opportunities for research in educational technology, AI-in-the-loop requirements engineering, NLP-based requirement quality analysis, and HCI. *(Platform-level emergent objective; no direct UC or FR realizer — research opportunities arise from the platform as a whole rather than any one feature.)*

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

Risks to the success of the system or project — spanning business/adoption, technical/feasibility, and security/safety categories. Each carries a name-based `RI-<slug>` key: the identifier is the risk's name, not a position, so risks can be added or removed without renumbering. Each risk's mitigating requirement(s) or its explicit acceptance is traced in the [risk coverage matrix](../traceability.md#risk-coverage-matrix); a risk left unmitigated by software carries an inline `*(Accepted; …)*` marker (parallel to the business-rule `*(Advisory; not enforced by the system)*` carve-out) so a consciously accepted risk is distinguishable from a forgotten one. `/spec-build`'s risk-coverage check flags any risk that is neither traced to a mitigation there nor marked accepted here.

RI-adoption-resistance: Adoption Resistance: Students comfortable with Microsoft Word or Google Docs may resist structured tools, particularly if the application proves more confusing to use than the previous manual process.

RI-llm-reliability: LLM reliability: AI suggestions may occasionally conflict with instructor expectations.

RI-scalability: Scalability concerns: High usage in large capstone programs may stress infrastructure.

RI-over-reliance-on-ai: Over-reliance on AI: Students might depend too heavily on AI-generated content.

RI-cross-browser: Cross-browser/device inconsistency: the application may render or behave inconsistently across the supported browsers (Chrome, Safari, Firefox) and the range of student and instructor devices, degrading usability for some users. *(Accepted; the supported set is bounded by OE-supported-browsers and conformance is verified by manual cross-browser testing — no automated guard today.)*

RI-accessibility: Accessibility shortfall: the application may fail to meet WCAG 2.1 AA accessibility standards, leaving some users unable to use it effectively and exposing the department to accessibility-compliance complaints (addressed by the accessibility quality attributes USE-wcag-aa / UI-wcag-aa in the Software Requirements Specification).

RI-cloud-cost: Cloud cost ownership: if the system is deployed on a cloud service provider, the Computer Science Department must budget for the annual cloud fees. *(Accepted; the Computer Science Department owns the cloud budget — a business decision with no software mitigation.)*

RI-data-breach: Data breach: the students' peer evaluation data may be targeted by attackers (addressed by the security quality attributes in the Software Requirements Specification).

RI-narrow-applicability: Narrow applicability: the application might prove too specialized for the TCU senior design course to be reused in other courses. *(Accepted; a reuse/adoption risk with no software mitigation in the MVP — post-MVP template customization (FR-TPL-customize) partially broadens applicability.)*

## **Business Assumptions and Dependencies**

Assumptions and dependencies are **one team-wide namespace** (`AS-*` / `DE-*`) deliberately **authored in two homes, partitioned by altitude**: the *business-level, environmental, and organizational* items live here; the *architecture-, technical-, and integration-level* ones live in the SRS's [Assumptions and Dependencies](software-requirements-specification.md#assumptions-and-dependencies). Each key is a name-based slug addable in either home without renumbering — but the namespace is shared, so each slug is **unique across both homes** and the two homes must **not state the same assumption under different keys**. `/spec-build`'s *cross-home assumption consistency* check guards this.

AS-internet-access: Students and instructors have stable internet access.

AS-formal-docs-required: Courses already require formal documentation.

AS-azure-available: Microsoft Azure infrastructure is available and supported.

AS-llm-cost-effective: LLM APIs (e.g., OpenAI) remain available and cost-effective.

AS-faculty-buy-in: Faculty buy-in will be sufficient for pilot use.

AS-maintainable-stack: The system uses technologies the client already knows and can maintain after the product is delivered.

# **Stakeholder Profiles and User Descriptions**

## **Stakeholder Profiles**

| Stakeholder                                                      | Major value or benefit from this product                                                                                                                               | Attitudes                                                                                                              | Major features of interest                                                                                                          | Constraints                                                                     | End user or not? |
| ---------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- | ---------------- |
| Undergraduate CS Students (Software Engineering & Senior Design) | Clear guidance on writing high-quality requirements; reduced confusion; structured templates; AI feedback; improved grades; fewer errors; professional-grade documents; one place to submit weekly activity reports and peer evaluations without uploading or downloading files | Generally willing to use new tools if easy-to-learn; expect modern UI; may resist if overly rigid; variable motivation | Smart editors, LLM assistance, auto-completeness checking, glossary linking, use case templates, version history, weekly activity report and peer evaluation submission                    | Limited experience with requirements engineering; time pressure; learning curve | Yes              |
| Software Engineering & Senior Design Instructors                 | Teach requirements effectively; efficient grading; consistent formatting; Ability to enforce standards; dashboards for progress; reduced manual review; faster peer-evaluation grading and clearer insight into team dynamics                 | Strongly supportive if tool reduces grading burden; expect accuracy and reliability                                    | Template customization, LLM tutoring, example patterns, traceability, dashboards, completeness metrics, requirement quality scoring, WAR review, peer-evaluation report generation | Limited time; large class sizes; need reliable, accurate checks                 | Yes              |
| Course Admin (course creator)                                    | Efficient course section and team setup; reliable roster and access management; one-click provisioning of each team's documents from built-in templates                | Wants minimal-friction setup; values reliability, correctness, and auditability                                        | Course section / team creation, roster and enrollment management, instructor invitations, template assignment, document provisioning | Owns a single course and is also an Instructor of it; limited time; needs correctness and an audit trail | Yes              |
| Client (project sponsor)                                         | A clearer, more complete specification of the system they asked the team to build; fewer misunderstandings surfaced late; better interview questions from a better-prepared team; a professional-quality document bundle to review and sign off on | Non-technical; busy; cares about the delivered product, not the tooling; expects the team to drive the conversation | Exported requirement documents for review; the elicitation question lists the team brings to interviews (experienced indirectly, through the team) | Limited availability; little or no requirements-engineering background; does not use the tool directly | No               |
| CS Department / faculty sponsor                                  | A modernized, reusable senior-design platform that raises requirements quality and reduces grading burden across cohorts; a basis for educational-technology research                | Supportive if costs stay predictable and the tool serves the curriculum; sensitive to recurring cloud spend (RI-cloud-cost) | Adoption and learning-outcome trends across course sections; cost and maintainability of the deployment | Owns the cloud budget and must fund annual fees (RI-cloud-cost); relies on Azure infrastructure being available (AS-azure-available); needs the system maintainable after delivery | No               |

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

**Project Pulse** is a web application for the senior design course, deployed as a single platform (a Vue.js SPA served by a Spring Boot REST API over a shared relational database). It provides course, course section, team, and user management, authentication, and email notifications, and on that foundation delivers two capability areas: the weekly activity report and peer evaluation workflows that track student performance, and the Requirements Authoring & Management (RAM) environment — structured templates, validation, AI-assistant guidance, collaboration, and instructor evaluation tools — for authoring software requirements.

## **Major Features / Scope**

Project Pulse provides a modern environment for running the senior design course — tracking student performance through weekly activity reports and peer evaluations, and authoring, validating, and managing software requirements. The features below describe the major functional capabilities at a high level. Each carries a flat name-based `FEAT-<slug>` identifier in its heading (parallel to the other requirement-ID spaces, and independent of reading position — inserting or reordering a feature renumbers nothing); downstream docs cite a feature by that handle rather than by prose name. Detailed behavioral descriptions, actor interactions, and exception scenarios are defined in the accompanying [Use Cases document](use-cases.md); the feature ↔ use-case-area map is maintained in [traceability.md](../traceability.md), keyed to the `FEAT-<slug>` IDs. The course-management and performance-tracking features come first, followed by the requirements-authoring (RAM) features.

### *FEAT-administration: Administration and Course Management*

The system supports secure authentication, role-based access control, and FERPA-compliant data storage. It provides course, course section, team, student, and instructor management for the whole platform: the course admin (the user who created the Course) sets up its course sections and project teams, manages rosters and document access, invites instructors, configures active weeks, and provisions each team's requirement documents from the built-in templates; instructors configure their course sections' teaching context and AI assistants and run the review-and-feedback workflow.

### *FEAT-rubric-management: Rubric Management*

Course admins author the peer-evaluation rubric: a weighted set of criteria that drives evaluation scoring. Rubrics and their criteria are managed centrally — created, edited, found, and deleted — and assigned to course sections, so different sections can use different rubrics and the same criterion can appear in multiple rubrics. The rubric is the contract the peer-evaluation workflow scores against.

### *FEAT-weekly-activity-reports: Weekly Activity Reports*

Each week, students record their work as a weekly activity report — for each activity, its category, the planned activity, a description, hours planned, actual hours taken, and status. Teammates and instructors can review a team's reports, and the system generates a WAR report for a team or for an individual student. This replaces the shared-spreadsheet workflow and fosters communication within the team.

### *FEAT-peer-evaluations: Peer Evaluations*

During active weeks, students evaluate their teammates against a rubric of weighted criteria, optionally adding public comments (shared with the evaluatee) and private comments (visible only to the instructor). The system aggregates scores and comments, generates a peer-evaluation report for the entire course section and per-student reports, and lets each student view her own results. Submission windows, edit rules, and visibility are governed by the business rules in [business-rules.md](business-rules.md) (BR-active-weeks, BR-evaluation-editable-until-close, BR-evaluation-submission-window, BR-evaluation-visibility).

### *FEAT-template-management: Template Management*

The tool includes built-in structured document templates (based on Wiegers & Beatty's Software Requirements 3rd Edition Book) for Vision and Scope, Use Cases, Glossary, Business Rules, SRS, and other requirement documents. These templates define the structure of each document and give students clear expectations for required document sections, content, and writing styles and standards.

Course admins or instructors can also create new structured document templates based on industry standards or course needs, including:

- Document section structure and ordering
- Required vs. optional fields
- Examples and instructional prompts
- Writing styles, terminology rules, and validation settings
- Grading rubrics tied to specific document sections

This enables flexibility across methodologies and supports instructor-specific teaching goals. The initial release ships with fixed, built-in templates; template customization is post-MVP — see [MVP Scope](#mvp-scope-initial-classroom-deployment).

### *FEAT-glossary-consistency: Glossary and Terminology Consistency*

Each project maintains a shared Project Glossary — the team's authoritative vocabulary for actors, domain concepts, and data entities referenced across every requirement document. Students view, search, create, edit, rename, and delete glossary terms in one place, and the same terms then flow into smart-editing assistance and ReqLint validation, where they enforce consistent naming as those terms appear in Vision and Scope, Use Cases, Business Rules, and the SRS. Renaming a glossary term updates references project-wide (safe rename), so the glossary stays the single source of truth and synonym drift is prevented across documents.

### *FEAT-document-authoring: Document and Use Case Authoring*

The primary authoring surface for the requirement documents. Students browse the team's documents, open a section-based document (Vision and Scope, Glossary, Business Rules, SRS) and edit its sections through a template-driven editor that follows the structure, required fields, and example prompts the chosen template defines. Use cases are first-class artifacts in their own catalog — students browse, view, create, and edit them through a use-case-shaped form covering actors, trigger, main success scenario, extensions, and associated information. Edits are protected by per-section / per-use-case pessimistic locking and autosaved as students work, so teams can divide and conquer documents without colliding.

### *FEAT-graph-model: Graph-First Requirements Model*

The tool is not a document editor intended to replicate Google Docs or Microsoft Word. Instead, RAM is model-first: it stores requirements as a connected domain model (a requirements graph) where nodes represent atomic requirement artifacts — each meaningful requirement element (objectives, features, use cases, functional requirements, glossary terms) modeled as a first-class, uniquely identifiable artifact — and edges represent typed relationships between them. Traditional "documents" are generated and edited as structured views over this underlying model, and the same artifact can surface in more than one view without duplication. This enables capabilities that are difficult or impossible in text-first tools — "find all references" for any artifact, safe rename that propagates across every view, and change-impact analysis. *(MVP ships safe rename; interactive graph visualization and change-impact analysis are post-MVP — see [MVP Scope](#mvp-scope-initial-classroom-deployment).)*

### *FEAT-traceability: Full Requirements Traceability*

The tool supports bidirectional traceability across all major requirement types, enabling students and instructors to see how business goals flow down into user requirements and functional specifications. Users can link:

- Business Requirements → Product Features
- Product Features → Use Cases (User Requirements)
- Use Cases → Functional Requirements
- Functional Requirements → Constraints, Data Requirements, Quality Attributes
- Business Rules → Use Cases / Functional Requirements
- Glossary Terms → All Requirements

The initial release supports creating, viewing, and navigating the typed links themselves — following them upstream and downstream to trace a requirement across levels; the traceability-matrix view, orphan and missing-link highlighting, and change-impact analysis are post-MVP — see [MVP Scope](#mvp-scope-initial-classroom-deployment). This lets students practice industry-standard traceability and helps instructors quickly evaluate completeness, coverage, and consistency across all requirement artifacts.

### *FEAT-validation: Smart Editing and Validation (ReqLint)*

The tool includes ReqLint, a lightweight rule-based validation engine inspired by code linters such as ESLint. ReqLint is non-AI and performs deterministic checks that enforce structure, completeness, and writing standards. Typical checks include:

- Incorrect formatting (e.g., missing "shall" statements in functional requirements, missing the actor in use case steps)
- Usage of vague verbs (e.g., "manage", "support") or subjective adjectives (e.g., "quick", "user-friendly")
- Ambiguous or non-testable requirement shapes
- Glossary and actor name consistency (e.g., usage of terminology not defined in the Glossary)
- Identifier and cross-reference correctness

A central and shared glossary is integrated into ReqLint to maintain consistent terminology for actors and domain concepts across all documents. ReqLint ensures students meet template and writing standards before any AI guidance is applied.

The MVP provides deterministic ReqLint validation together with glossary-term suggestion; broader smart-editing auto-suggestion and auto-complete are post-MVP — see [MVP Scope](#mvp-scope-initial-classroom-deployment).

### *FEAT-ai-assistance: AI-Assisted Guidance and Feedback*

Beyond rule-based validation, RAM provides AI assistance through a set of deliberately Socratic assistants whose purpose is educational: they coach students to author high-quality requirements and to communicate with non-technical clients, rather than producing finished requirements. Each assistant behaves according to its instructor-authored assistant instructions — the per-assistant role, persona, and boundaries — and draws on the course section's teaching context (the standards, common student mistakes, and thinking order the course teaches); instructors enable or disable each assistant per course section.

The assistants are grounded in the team's own project material. At the start of a project the client provides pitch materials — typically a slide deck and a short brief — describing the background, stakeholders, problem, users, objectives, desired functionality, possible solutions, prototypes, and a candidate tech stack. Students import these into RAM as **project source material**: the input the team works from. RAM stores the materials, extracts their text, and makes them available as context to the assistants — most directly to the elicitation assistant, which uses them in its gap analysis to help the team prepare client-interview questions. Because the pitch goes stale as the project evolves, later-authored, verified requirements take precedence, and a student can exclude the project source material from an elicitation session entirely. Project source material is a project input, not authored requirement content, and is not itself graded.

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

### *FEAT-collaboration: Collaboration and Document Workflow*

Student teams collaborate through document-section-level editing and commenting. The tool supports submission-and-review workflows that allow students to submit drafts, receive instructor feedback, and revise their work iteratively. The initial release coordinates teammates through per-section pessimistic locking and comment threads; real-time collaboration — live presence and concurrent co-editing — is post-MVP, see [MVP Scope](#mvp-scope-initial-classroom-deployment).

The initial release keeps authorship metadata (the creator/editor and timestamps recorded on every authored item); document version history — checkpointing each save, viewing prior versions, and restoring them — is post-MVP, see [MVP Scope](#mvp-scope-initial-classroom-deployment).

### *FEAT-instructor-review: Instructor Dashboard, Feedback, and Grading*

Instructors will have access to dashboards summarizing team progress, document completeness, and validation results. Integrated grading tools enable rubric-based evaluation aligned with the selected templates, as well as inline comments and structured feedback. This improves grading consistency and reduces review effort.

**MVP scope:** the initial release provides only the review-and-feedback workflow — an instructor opens a submitted document, leaves inline comments, and either accepts it or returns it for revision (governed by the business rules in [business-rules.md](business-rules.md)). Progress/completeness dashboards and rubric-based grading are post-MVP — see [MVP Scope](#mvp-scope-initial-classroom-deployment).

### *FEAT-export: Export and Delivery*

Students can export requirement documents to professional-quality PDF, Word (DOCX), or Markdown formats that follow the structure of the chosen template (Markdown prioritizes structure and traceability over visual styling, and supports downstream code generation). Exported documents are suitable for client or stakeholder review in senior design and capstone projects.

## **MVP Scope (Initial Classroom Deployment)**

The boundary below scopes the Requirements Authoring & Management (RAM) release: it is the single authority for what the initial RAM release includes, and the per-feature notes above defer to it. The course-operations and performance-tracking workflows — course / course section / team / roster management, weekly activity reports, and peer evaluations — fall outside this RAM boundary. The MVP focuses on correctness, traceability, collaboration, coaching, and export quality.

**In scope (initial RAM release):**

- Structured, collaborative authoring of the requirement documents (Vision and Scope, Glossary, Use Cases, Business Rules, SRS) using fixed, built-in templates.
- The requirements graph with typed artifact links, trace navigation, and safe rename (see the Use Cases document, *Artifact Links and Tracing*: UC-LNK-view-links through UC-LNK-trace-requirement).
- Deterministic ReqLint validation together with glossary-term suggestion.
- The Socratic AI assistants that coach requirements authoring and client elicitation — the elicitation, critique, tutor, structuring, and client role-play assistants — reached through the project assistant, the project-level conversational front door that orients students and routes them to the right assistant.
- Project source material import as context for the assistants.
- Teammate coordination through per-section pessimistic locking and comment threads (UC-COL-add-comment, UC-COL-resolve-comment), and the submission-and-review workflow — an instructor opens a submitted document, leaves inline comments, and accepts it or returns it for revision (UC-REV-submit-for-review, UC-REV-review-documents).
- Authorship metadata (creator/editor and timestamps) on every authored item.
- Export to PDF, Word (DOCX), and Markdown.

**Out of scope (deferred to a future release):**

- Free-form AI generation of finished requirements (the drafting assistant is off by default in course use).
- Template customization by a course admin or instructor.
- Broader smart-editing auto-suggestions (actors, requirement patterns, cross-document links) and auto-complete beyond glossary-term suggestion.
- Interactive traceability-matrix generation, orphan and missing-link highlighting, and change-impact analysis (the typed links and trace navigation themselves remain in scope).
- Interactive graph visualization.
- Real-time collaboration — live presence and concurrent co-editing (UC-COL-collaborative-edit).
- Document version history — checkpointing each save, viewing prior versions, and restoring them (authorship metadata is kept).
- Advanced analytics and instructor progress/completeness dashboards, and automated/rubric-based grading (the review-and-feedback workflow remains in scope).

## **Deployment Considerations**

- Deploy to Microsoft Azure App Service
- OpenAI API for LLM integration
- CI/CD using GitHub Actions
- Load testing for peak usage
