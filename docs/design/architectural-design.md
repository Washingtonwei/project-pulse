# **Architectural Design — Project Pulse Platform**

> **Design-of-record for the Project Pulse platform** — the one application that comprises the foundation and performance-tracking features (weekly activity reports, peer evaluations, courses/course sections/teams) and the RAM module. It is the single architecture-of-record for the whole product: the platform-wide context/container views and conventions **plus** the RAM module's component view and cross-cutting subsystems (folded into the Building Block View and Crosscutting Concepts below).
>
> Structure: this document follows the **arc42** template (Starke & Hruschka), using **C4** for the context and building-block views. The section names and order are arc42's; numbering is applied at export.
>
> See: [`../README.md`](../README.md) (docs overview), [`../requirements/software-requirements-specification.md`](../requirements/software-requirements-specification.md) (the SRS this design realizes), [`../CLAUDE.md`](../CLAUDE.md) (spec-doc authoring conventions).

## **Introduction and Goals**

Project Pulse was built **before RAM**: the weekly-activity-report, peer-evaluation, and course/course-section/team functionality — together with security, the API conventions, and the deployment pipeline — came first as the working application. **RAM was a separate project, merged in later** to reuse this same course/section/team/security/auth infrastructure. So Project Pulse is the platform, RAM is a module within it, and **the conventions here belong to the platform** — the requirements specs cite them rather than restate them.

This doc is the platform's **architecture-of-record**: the structure, conventions, decisions, cross-cutting concerns, runtime/deployment, and known risks every module inherits or is bounded by. It is **not** named after a use-case area and does not change when one feature is added — it changes when the platform architecture does.

### *Requirements Overview*

The platform's functional requirements live in the **requirements specs** under [`../requirements/`](../requirements/) (a Wiegers/Beatty-style SRS plus use cases, glossary, and business rules), not here. This document realizes those requirements and does not restate them. In brief, the platform delivers weekly activity reporting, peer evaluation, instructor dashboards, and course/section/team management, and — through the RAM module — collaborative requirements authoring (documents, use cases, glossary, traceability, validation, and AI-assisted review).

### *Quality Goals*

> The architecturally significant quality attributes that drive the design, in priority order. The full quality-attribute *requirements* live in the requirements specs; this states the architecture's **response** to the ones that most shape the structure, and [Quality Requirements](#quality-requirements) makes them measurable.

| # | Quality goal | Why it drives the architecture | How the architecture meets it |
|---|---|---|---|
| 1 | **Security & privacy (FERPA)** | The platform holds student educational records (WARs, peer evaluations, scores, requirements). Unauthorized disclosure is the top risk and a regulatory (FERPA) constraint. | JWT auth (RSA keypair); URL-level rules in `SecurityConfiguration` **plus** fine-grained `AuthorizationManager` beans enforcing ownership/membership (a student sees only their team's data; an instructor only their assigned course sections); role hierarchy `admin > instructor > student`; JPA auditing stamps created/modified-by for accountability; `prod` secrets in Azure Key Vault; single-tenant deployment boundary. |
| 2 | **Maintainability & extensibility** | The codebase is extended continuously — RAM was merged in, senior-design students will extend it, and features are added spec-first via `/design`→`/implement`. New code must stay safe and consistent. | DDD one-bounded-context-per-package vertical slices isolate change; uniform conventions (the `Result` envelope, `Converter<S,T>` DTOs, no Lombok/MapStruct, `/api/v1`) make every slice look alike so a contributor pattern-matches; cross-cutting machinery is centralized in `system`/`security`/`user` so features don't reinvent it; Flyway makes schema change auditable. |
| 3 | **Usability / low friction** | Non-expert students must submit WARs and peer evaluations and author requirements quickly — friction directly reduces the frequent participation the platform exists to encourage. | SPA (Vue) for a responsive single-page UX; the uniform `Result` envelope + shared Axios instance give consistent client-side error handling and transparent auth/`401` redirect; RAM autosave + section-level locking prevent lost work and edit collisions; `WeeklyReminderScheduler` emails nudge timely submission. |
| 4 | **Reliability & low operational burden** | A single instructor runs a course section with no dev/ops team; the system must deploy and run simply and behave predictably. | One deployable unit (SPA bundled into the Spring Boot jar → one container → one Azure Web App) with nothing to orchestrate; staging slot for safe deploys; Flyway migrations applied at deploy for predictable schema; profile-scoped clocks for testable time; Testcontainers integration tests + `maven-build` PR checks guard regressions; Prometheus/Grafana/Zipkin for monitoring (local dev today; production telemetry is a gap — [TD-9](#risks-and-technical-debt)). **Accepted trade-off:** the single-instance simplicity (and the per-startup RSA key) caps horizontal scaling — deliberately traded for low ops at the current scale. |

### *Stakeholders*

| Stakeholder | Concern |
|---|---|
| **Students** | Submit WARs and peer evaluations, author requirements; expect low-friction workflows, fair evaluation, and privacy of their records. |
| **Instructors / course admins** | Monitor team progress, review submissions and requirements, manage courses/sections/teams; the primary operators. |
| **Department / institution** | FERPA compliance and stewardship of student educational records. |
| **Developers & maintainers** (incl. senior-design students who extend the codebase) | Clear conventions and maintainability so features can be added safely. |
| **Educator / researcher** (the author) | Use the platform and its docs as a teaching exemplar and research artifact. |

## **Architecture Constraints**

Constraints the architecture must honor, gathered from the requirements, the institution, and the existing platform:

- **Regulatory** — the system holds student educational records, so **FERPA** governs access, disclosure, and retention (Quality Goal #1).
- **Organizational** — a single instructor operates a course with **no dedicated dev/ops team**; deployment is **single-tenant** (one institution per deployment).
- **Platform-given** — RAM is a **module inside a fixed platform**: it must reuse the existing course/section/team/auth/email infrastructure and the platform conventions, not fork or duplicate them. The platform was built before RAM, so those conventions predate and bind RAM.
- **Process** — requirements are authored as durable specs (Markdown under `docs/requirements/`) and drive the design.
- **Technology stack (prescribed):**
  - **Backend** — Java 21, Spring Boot 4.0, Maven; Spring Security, Spring Data JPA, Flyway.
  - **Frontend** — Vue 3 + Vite + TypeScript SPA; Element Plus, SCSS, Pinia, Chart.js (vue-chartjs), TipTap (rich text, used by RAM); Cypress for E2E.
  - **Data & infra** — relational DB (MySQL 8 in dev via Docker Compose, alongside Mailpit, Prometheus, Grafana, Zipkin); deployed on Azure.

## **Context and Scope**

```mermaid
C4Context
    title System Context Diagram for Project Pulse

    Person(instructor, "Instructor", "Senior design course instructor")
    Person(student, "Senior Design Student", "Enrolled in the course")

    System(pulse, "Project Pulse", "Hosts WARs, peer evaluations, and the RAM requirements module")

    System_Ext(gmail, "Gmail", "Email system")
    System_Ext(llm, "LLM Service", "AI-assisted requirement review")

    Rel_R(instructor, pulse, "Manages courses;<br/>reviews requirements")
    Rel_R(student, pulse, "Submits work;<br/>authors requirements")
    Rel_R(pulse, gmail, "Sends emails using")
    Rel_D(gmail, student, "Sends emails to")
    Rel_D(pulse, llm, "Requests AI review")

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

The Level 1: Context Diagram for the Project Pulse system provides a high-level overview of its interactions with users and external systems. Project Pulse is the central platform for senior-design course delivery: instructors create courses, author Weekly Activity Report (WAR) and peer evaluation templates, and review submissions, while students submit WARs, complete peer evaluations, and view scores and feedback. The Requirements Authoring & Management (RAM) module runs inside this same platform, where students and instructors author, link, and validate requirements as a connected graph of atomic artifacts. Project Pulse integrates with two external systems: the Gmail system, which delivers automated email notifications (reminders, updates) to students and instructors — the context diagram shows the student notification path as representative — and an external LLM service (e.g., OpenAI), which the RAM module calls for AI-assisted requirement review. This diagram highlights the instructor and student as primary users, the central functionality of Project Pulse including the RAM module, and the platform's reliance on Gmail for communication and the LLM service for AI assistance, offering a clear picture of the system's operational scope and interactions.

## **Solution Strategy**

The platform's strategy in a few load-bearing moves — each elaborated in [Architecture Decisions](#architecture-decisions) and serving the Quality Goals:

- **One deployable, modular monolith** (KD-1) — the SPA bundled into the Spring Boot jar, one container — for low operational burden (Quality Goal #4).
- **DDD vertical slices + uniform conventions** (the `Result` envelope, `Converter` DTOs, no Lombok — KD-5) — for maintainability and a low learning curve (Quality Goal #2).
- **RAM as a module on the shared platform** (KD-2) — reuse identity, RBAC, the org model, and email rather than build a second system.
- **One relational store** for both modules (KD-3) — a single backup/migration/compliance surface.
- **Stateless, self-issued JWT** (KD-4) **+ fine-grained `AuthorizationManager`s** — least-privilege access to regulated data (Quality Goal #1).
- **Documented per arc42 + C4** — a recognized structure for teaching and review.

## **Building Block View**

The platform's building blocks at two levels: the **containers**, and the **components** inside the REST API container.

### *Containers*

```mermaid
C4Container
    title Container Diagram for Project Pulse

    Person(student, "Senior Design Student", "Enrolled in the course")

    Container_Boundary(pulse, "Project Pulse") {
        Container(spa, "SPA", "Vue.js", "Course-management UI + RAM authoring views (graph, editor, ReqLint, AI panel)")
        Container(api, "REST API Application", "Java / Spring Boot", "Course-management & RAM APIs (graph, ReqLint, AI proxy)")
        ContainerDb(db, "Database", "Relational DB", "WARs, peer evals, and RAM artifacts/links/documents")
        ContainerDb(blob, "Blob Storage", "Azure Blob Storage", "Uploaded project source material (PDF/PPTX)")
    }

    System_Ext(gmail, "Gmail", "Email system")
    System_Ext(llm, "LLM Service", "AI-assisted requirement review")

    Rel_R(student, spa, "Uses", "HTTPS")
    Rel_D(spa, api, "API calls", "JSON/HTTPS")
    Rel_D(api, db, "Reads & writes", "JDBC")
    Rel_D(api, blob, "Stores & reads files", "HTTPS")
    Rel_R(api, gmail, "Sends email", "SMTP")
    Rel_R(api, llm, "Requests AI review", "HTTPS")
```

The Level 2: Container Diagram for the Project Pulse system provides a detailed view of its internal architecture, illustrating how the system components interact. The system is composed of three key containers — the **SPA (Single Page Application)**, the **REST API Application**, and the **Database** — supported by integration with the **Gmail System** for email communication and an external **LLM Service** (e.g., OpenAI) for AI-assisted requirement review. The **SPA**, built with Vue.js, is delivered to users' browsers and provides the interface for both the course-management workflows (submitting WARs and peer evaluations) and the RAM module's requirements authoring views: graph navigation, document editing, the ReqLint validation sidebar, and the AI assistant panel. The **REST API Application**, implemented using Java and Spring Boot, delivers the SPA, processes REST API calls, and manages interactions with the **Database**; for the RAM module it exposes endpoints for the requirements graph, ReqLint validation, and an AI proxy to the LLM service. The **Database**, a relational database, stores course-management data (WARs and peer evaluation submissions) alongside the RAM module's requirement artifacts, links, documents, and document sections, with CRUD operations executed through the REST API. **Azure Blob Storage** holds one kind of data the relational database deliberately does not — the large binary files of uploaded **project source material** (PDF/PowerPoint); the database keeps only a reference to each blob plus the server-side-extracted text (see the Data architecture section). The REST API integrates with the **Gmail System** over SMTP to send automated notifications and with the external **LLM Service** to support AI-assisted review.

### *Components*

This view zooms into the **REST API Application** to show the platform's internal structure: one component per DDD bounded context, on top of the shared cross-cutting packages. Each maps to a package under `backend/src/main/java/team/projectpulse/`. RAM adds its own components (`ram/*`) beside these on the same shared base — see [RAM components](#ram-components) below.

```mermaid
C4Component
    title Component Diagram — Project Pulse foundation & performance-tracking components inside the REST API Application

    Container(spa, "SPA", "Vue.js", "Course management UI: WARs, peer evaluations, dashboards")

    Container_Boundary(api, "REST API Application (Spring Boot)") {
        Component(activity, "activity", "Controller / Service / Repository", "Weekly Activity Reports")
        Component(evaluation, "evaluation", "Controller / Service / Repository", "Peer evaluations")
        Component(rubric, "rubric", "Controller / Service / Repository", "Evaluation rubrics")
        Component(org, "course · section · team", "Controller / Service / Repository", "Courses, course sections, teams — the org/enrollment model")
        Component(people, "student · instructor", "Controller / Service / Repository", "Course participants & roles")
        Component(shared, "Shared platform packages", "system · security · user", "Result / StatusCode / ExceptionHandlerAdvice, JWT auth + AuthorizationManagers, JPA auditing, EmailService, clock & profile config")
    }

    ContainerDb(db, "Database", "Relational DB", "WARs, peer evals, courses/sections/teams, users")
    System_Ext(gmail, "Gmail", "Email system")

    Rel_D(spa, activity, "JSON/HTTPS")
    Rel_D(spa, evaluation, "JSON/HTTPS")
    Rel_D(spa, org, "JSON/HTTPS")
    Rel(activity, shared, "builds on")
    Rel(evaluation, shared, "builds on")
    Rel(rubric, shared, "builds on")
    Rel(org, shared, "builds on")
    Rel(people, shared, "builds on")
    Rel_D(activity, db, "JDBC")
    Rel_D(evaluation, db, "JDBC")
    Rel_D(org, db, "JDBC")
    Rel(shared, gmail, "Sends email", "SMTP")
```

On the **SPA** side the layering is uniform across the app: feature pages call a per-domain API client (`frontend/src/apis/<feature>/`) over a shared Axios instance that attaches the JWT Bearer token, unwraps the `Result` envelope, and redirects to login on `401`. Pinia stores (`token`, `userInfo`, …) hold cross-cutting state; the router enforces `requiresAuth` / role guards.

### *RAM components*

This view zooms into the **REST API Application** to show the **RAM module's** internal structure: one component per DDD bounded context, sitting on the same shared platform packages (`system` · `security` · `user`) the foundation and performance-tracking components build on. Each maps to a package under `backend/src/main/java/team/projectpulse/ram/` — a Level-2 area design doc designs the inside of one of these boxes; this diagram fixes the boxes and how they relate.

```mermaid
C4Component
    title Component Diagram — RAM components inside the REST API Application

    Container(spa, "SPA", "Vue.js", "RAM authoring views: Documents, Document Editor, Use Cases, Glossary, graph & traceability, ReqLint, Collaboration, Review, Export, AI panels")

    Container_Boundary(api, "REST API Application (Spring Boot)") {
        Component(doc, "document", "Controller / Service / Repository", "Requirement documents & document sections, templates/provisioning, section-level pessimistic locking, autosave")
        Component(req, "requirement", "Controller / Service / Repository", "Requirement artifacts, artifact links & tracing, key-prefix sequences")
        Component(uc, "usecase", "Controller / Service / Repository", "Use case artifacts: main steps, extensions, locking")
        Component(glo, "glossary", "Controller / Service", "Glossary terms & terminology invariants")
        Component(val, "validation", "Controller / Service", "ReqLint structural & consistency checks")
        Component(col, "collaboration", "Controller / Service / Repository", "Comment threads (built); real-time presence/broadcast is a deferred future layer")
        Component(rev, "review", "Controller / Service / Repository", "Review & submission workflow")
        Component(exp, "export", "Controller / Service", "Export rendering (PDF/DOCX/Markdown); project-source-material upload, storage & text extraction")
        Component(ai, "ai", "Controller / Service", "AI configuration, AI assistants & LLM proxy")
        Component(shared, "Shared platform packages", "system · security · user", "Result / StatusCode / ExceptionHandlerAdvice, JWT auth + AuthorizationManagers, JPA auditing (authorship), EmailService")
    }

    ContainerDb(db, "Database", "Relational DB", "RAM artifacts, links, documents, sections, comments, AI config")
    ContainerDb(blob, "Blob Storage", "Azure Blob Storage", "Uploaded project source material")
    System_Ext(llm, "LLM Service", "AI-assisted requirement review")

    Rel_D(spa, doc, "JSON/HTTPS")
    Rel_D(spa, req, "JSON/HTTPS")
    Rel_D(spa, uc, "JSON/HTTPS")
    Rel_D(spa, glo, "JSON/HTTPS")
    Rel_D(spa, val, "JSON/HTTPS")
    Rel_D(spa, col, "JSON/HTTPS")
    Rel_D(spa, rev, "JSON/HTTPS")
    Rel_D(spa, exp, "JSON/HTTPS")
    Rel_D(spa, ai, "JSON/HTTPS")
    Rel(doc, shared, "builds on")
    Rel(req, shared, "builds on")
    Rel(uc, shared, "builds on")
    Rel(glo, shared, "builds on")
    Rel(val, shared, "builds on")
    Rel(col, shared, "builds on")
    Rel(rev, shared, "builds on")
    Rel(exp, shared, "builds on")
    Rel(ai, shared, "builds on")
    Rel_D(doc, db, "JDBC")
    Rel_D(req, db, "JDBC")
    Rel_D(uc, db, "JDBC")
    Rel_D(col, db, "JDBC")
    Rel_D(rev, db, "JDBC")
    Rel_D(exp, db, "JDBC")
    Rel_R(exp, blob, "Stores & reads files", "HTTPS")
    Rel_D(ai, db, "JDBC")
    Rel_R(ai, llm, "AI proxy", "HTTPS")
```

These are the RAM module's bounded contexts — each maps to a package under `ram/` and is the subject of a Level-2 area design doc that designs the inside of one box. The boundaries for areas not yet designed are **provisional**: they are drawn here from the use-case areas so the map is complete, but the first `/design` of an area validates a boundary against the code and **may revise this diagram** (splitting, merging, or re-homing a component, or moving a subsystem owner), recording the change as part of that run. The `ai` component is the only one that reaches a third-party service: it proxies to the external LLM service for AI-assisted review. The `exp` component reaches the platform's own **Azure Blob Storage** to store and read uploaded project source material (see [Data architecture](#data-architecture)); every other component persists only through the relational database. On the **SPA** side the layering mirrors the rest of Project Pulse: RAM pages (`frontend/src/pages/ram/`) call a per-domain API client (`frontend/src/apis/ram/`) over the shared Axios instance that attaches the Bearer token and unwraps the `Result` envelope; that layering is a platform convention (see [Crosscutting Concepts](#crosscutting-concepts)), not redrawn per area. The build status of each component and subsystem is not tracked here — that is [`../traceability.md`](../traceability.md)'s job, per use case.

## **Runtime View**

> Key runtime scenarios — how the building blocks interact at run time. The physical topology is in [Deployment View](#deployment-view).

### *Authentication and an authorized request*

```mermaid
sequenceDiagram
    actor U as User (browser)
    participant SPA
    participant API as REST API
    participant DB
    U->>SPA: enter email + password
    SPA->>API: POST /api/v1/users/login (HTTP Basic)
    API->>DB: load user, verify BCrypt-12 hash
    API-->>SPA: Result { token: JWT (RSA-2048, 2h) }
    SPA->>SPA: store token (Pinia), set Bearer header
    SPA->>API: GET /api/v1/... (Bearer JWT)
    API->>API: verify JWT, then AuthorizationManager (ownership/membership)
    API-->>SPA: Result { data }
```

### *WAR submission (representative course-management flow)*

1. Visit the Project Pulse Website: The Senior Design student begins by accessing the Project Pulse system through their browser at the URL https://projectpulse.team.
2. Deliver the SPA to the student's Browser: The REST API Application (built using Java and Spring Boot) serves the Single Page Application (SPA, built with Vue.js) to the student's browser. This provides the user interface that students interact with.
3. Submit WARs and Peer Evaluations: The student uses the SPA to complete and submit Weekly Activity Reports (WARs) and peer evaluations through the interface.
4. Make REST API Calls to the Backend: The SPA communicates with the REST API Application by making REST API calls to process and handle the submissions from the student. These calls allow the backend to manage the application's logic and facilitate data processing.
5. CRUD Operations with the Database: The REST API Application performs CRUD (Create, Read, Update, Delete) operations on the Database, which is a relational database. The database securely stores the submitted WARs and peer evaluations.
6. Send Emails via SMTP: The REST API Application interacts with the Gmail system using SMTP to send automated email notifications (e.g., reminders, submission confirmations) to the student or instructor, as necessary.
7. Receive Emails: Finally, the student (or instructor) receives email notifications generated by the Gmail system, completing the interaction loop.

RAM-specific flows (graph navigation, document editing, ReqLint validation, and AI-assisted review) follow the same SPA → REST API → Database path, with the REST API additionally proxying requests to the external LLM service; the LLM round-trip degrades gracefully when the service is unavailable (QS-5).

## **Deployment View**

> The physical runtime topology, the build/release pipeline, and the scaling posture. The logical structure is in the views above; this is where the system actually runs, and why it is currently single-instance.

```mermaid
flowchart LR
    browser["Student / Instructor<br/>Browser"]
    subgraph azure["Azure"]
        subgraph webapp["Azure Web App (single instance)"]
            slot["Production slot<br/>1 container: Spring Boot jar<br/>(REST API + bundled Vue SPA)"]
            staging["Staging slot<br/>(deploy target)"]
        end
        db[("Azure Database<br/>for MySQL")]
        blob[("Azure Blob Storage<br/>(project source files)")]
    end
    gmail["Gmail<br/>(SMTP)"]
    llm["LLM Service<br/>(HTTPS)"]

    browser -->|HTTPS| slot
    slot -->|JDBC| db
    slot -->|Blob SDK / HTTPS| blob
    slot -->|SMTP| gmail
    slot -->|HTTPS| llm
    staging -. swap .-> slot
```

### *Topology*

- One **Azure Web App** runs a **single container** — the Spring Boot jar serving both the REST API and the bundled Vue SPA (KD-1). It talks to one **Azure Database for MySQL** over JDBC, **Azure Blob Storage** (uploaded project source material) over HTTPS, **Gmail** over SMTP, and the external **LLM** over HTTPS. Releases deploy to a **staging slot** and swap into production (pipeline below).

### *Build & release pipeline*

The whole platform deploys as one unit, RAM included. The CI pipeline (`azure-webapps-deploy.yml`, on push to `main`) builds the Vue frontend, copies the `dist` into `backend/src/main/resources/static/`, builds the Spring Boot jar, packages a Docker image, pushes it to GHCR, and deploys to an Azure Web App staging slot. PR checks (`maven-build.yml`) run `mvn package` (backend build + tests) on PRs to `main`. In production the single Spring Boot container serves both the REST API and the SPA. Schema changes ship as versioned Flyway migrations applied at deploy time.

### *Statefulness*

- The app is largely **stateless**: JWT auth with no server session (`STATELESS`), authoring locks persisted in the DB (not memory), and no application cache. The only per-instance in-memory state is the **RSA signing keypair** (generated at startup, KD-4).

### *Scaling model & path to multi-instance*

- Today the platform scales **vertically only** — one instance (Quality Goal #4; the accepted Scalability trade-off). It is close to horizontally scalable, but three things block it today:
  1. **Per-startup RSA key (KD-4):** each instance signs with its own key, so instances can't verify each other's tokens. *Fix:* externalize/persist the keypair (or a shared JWKS).
  2. **Scheduled jobs duplicate:** `WeeklyReminderScheduler` (`@Scheduled` cron) fires on every instance, so N instances send N reminder emails. *Fix:* single-execution coordination (ShedLock / leader election / a dedicated scheduler instance).
  3. **Otherwise stateless:** beyond (1)–(2) there is no sticky-session or in-memory-cache barrier — DB-backed locks and stateless auth already support multiple instances.
- Until those are addressed, run a single instance (tracked in Risks and Technical Debt).

### *Availability*

- Single instance ⇒ a single point of failure, with brief downtime on restart/deploy (mitigated by the staging-slot swap). External-dependency failures degrade gracefully (LLM down ⇒ authoring continues, QS-5; email is best-effort).

## **Crosscutting Concepts**

Conventions, shared machinery, and platform-wide concerns that cut across all building blocks.

### *Architectural conventions*

These are the **canonical** platform conventions — the normative source the root [`CLAUDE.md`](../../CLAUDE.md) and the spec-doc [`CLAUDE.md`](../CLAUDE.md) point to. Every module follows them.

- **API shape** — all endpoints under `/api/v1` (`api.endpoint.base-url`); every controller method returns the `Result` envelope (`flag`, `code`, `message`, `data`) — never a raw entity; errors are translated centrally by a global `@RestControllerAdvice` (`ExceptionHandlerAdvice`) into the same envelope with `StatusCode` constants.
- **Domain structure** — Domain-Driven Design: one bounded context per package, each owning its full vertical slice (entity → repository → service → controller → DTOs → `Converter<S,T>` → a `*SecurityService` or `*Specs` for dynamic queries). **No Lombok** — explicit getters/setters/constructors. **No MapStruct** — bidirectional DTO conversion via Spring `Converter<S,T>` beans.
- **Authorization** — JWT-based auth (RSA key pair generated at startup); URL-level rules in `SecurityConfiguration`'s filter chain **plus** fine-grained `AuthorizationManager` beans for ownership/membership checks. Role hierarchy `admin > instructor > student`.
- **Persistence & migrations** — relational DB via JPA. Schema is delivered as **Flyway** migrations (`backend/src/main/resources/db/migration/`). The `dev` profile uses `ddl-auto: create` + `DataInitializer` seed data; `staging`/`prod` use Flyway only (`prod` pulls secrets from Azure Key Vault).
- **SPA serving** — in production the Spring Boot app serves both the API and the built SPA from `static/`; `WebConfig` forwards non-API UI routes (registered for one-, two-, and three-segment paths) to `index.html` for client-side routing.

### *Cross-cutting subsystems*

Shared machinery every module reuses rather than reimplements — owned by the cross-cutting packages, not any one domain.

| Subsystem | Owner | Notes |
|---|---|---|
| Standard API envelope & error handling | `system` (`Result`, `StatusCode`, `ExceptionHandlerAdvice`) | Every controller returns `Result`; all exceptions funnel through the global advice |
| Authentication & RBAC | `security` (`SecurityConfiguration`, `authorizationmanagers/`) | JWT (RSA keypair at startup); URL rules + ownership/membership managers |
| Shared user model | `user` (`PeerEvaluationUser` base, password reset, invitation) | Common identity base for students/instructors; the auth subject for both modules |
| Authorship & auditing | `system` (JPA auditing, `PeerEvaluationUserAuditorAware`) | Created/modified-by metadata applied automatically across entities |
| Email / notifications | `system` (`EmailService`, `WeeklyReminderScheduler`) | Gmail over SMTP; scheduled reminders |
| Time & profiles | `system` (`DevClockConfig` / `StagingClockConfig` / `ProdClockConfig`) | Profile-scoped clocks for testable time |
| Dev seed data | `system` (`DataInitializer`) | `dev`-profile fixtures (the dev credentials) |

### *RAM cross-cutting subsystems*

Beyond the platform-wide machinery above, each **RAM** area builds on a small set of RAM-owned **shared subsystems** rather than reinventing them; a Level-2 area design should *plug into* the relevant row, not redesign it. The FR families are specified in the SRS's Non-Use Case Functional Requirements; "owner" is the package that provides the machine. (Per-use-case build status lives in [`../traceability.md`](../traceability.md), not here.)

| Subsystem | FR family | Owner | How an area plugs in |
|---|---|---|---|
| Section locking | `FR-LOCK-*` | `ram/document` (`DocumentSectionLock`), `ram/usecase` (`UseCaseLock`) | Acquire/release a lock on the authoring destination before edit (UC-DOC-2 / UC-DOC-6) |
| Document templates | `FR-TPL-*` | `ram/document/template` (`DocumentTemplateRegistry`) | Provision a document's sections from its `DocumentType` template |
| Collaboration | `FR-COL-*` | `ram/collaboration` | Attach comment threads to an artifact / destination (the built collaboration model). Real-time presence/broadcast (UC-COL-1, `FR-COL-*`, PER-1) is **deferred** — a future layer, not in the current design (see KD-6) |
| Glossary | `FR-GLO-*` | `ram/glossary` | Terminology lookups and invariants |
| Authorship & history | `FR-HIS-*` | `system` (JPA auditing, `PeerEvaluationUserAuditorAware`) | Inherited via JPA auditing — no per-area work |
| Notifications | `FR-NOT-*` | `system` (`EmailService`, `WeeklyReminderScheduler`) | Call `EmailService`; Gmail over SMTP |
| Security / RBAC | `FR-SEC-*` | `security` (`AuthorizationManager` beans) | Add a URL rule + an ownership/membership manager |
| Autosave | `FR-SAVE-*` | `ram/document` (section save) + client-side debounce | Persist edits through the document-section save endpoint |
| Validation (ReqLint) | `FR-VAL-*` | `ram/validation` | Deterministic structural checks (UC-VAL-1) |
| AI assistants | `FR-AI-*` | `ram/ai` (LLM proxy) | Proxy to the external LLM service |
| Export / Import | `FR-EXP-*` / `FR-IMP-*` | `ram/export` | Export rendering to PDF/DOCX/Markdown preserving template structure (UC-EXP-1/2); project-source-material upload (PDF/PPTX, allowlisted, ≤ 25 MB), storage, and server-side text extraction for AI context (UC-AI-1). Binary storage & extraction: see [Data architecture](#data-architecture) |

### *Security & Compliance*

> Security is Quality Goal #1: the platform holds student educational records regulated under **FERPA**. This is the consolidated security view — authentication, authorization, data protection, threat boundary — recording both the **controls in place** and the **known gaps** a production deployment must close. Mechanics live in `security/` and `user/`; this explains the design, it doesn't restate every rule.

#### **Trust boundary**

Single-tenant: one deployment serves one institution. The trust boundary is the Azure Web App — the Vue SPA and REST API are one origin (SPA served from the API jar), so there's no cross-origin trust in production. TLS terminates at the Azure edge; the app trusts the platform for transport encryption (it does not enforce HTTPS itself).

#### **Authentication**

- **Login:** HTTP Basic (email + password) → server issues a JWT (`AuthController` / `JwtProvider`). Passwords hashed with **BCrypt (strength 12)**.
- **Tokens:** self-issued JWTs, **RSA-2048** signature, **2-hour** expiry, claims `sub`, `userId`, space-delimited `authorities`. Stateless OAuth2 resource server (`SessionCreationPolicy.STATELESS`).
- **Provisioning:** invitation-gated registration (`user/userinvitation` — `validateUserInvitation` rejects any sign-up without a matching course-admin-issued invitation; see Known gaps); password reset via a one-time token valid **5 minutes**.
- **Known gaps:** no token **refresh** or **revocation/blocklist** — a token is valid for its full 2h (a leaked token can't be revoked; logout is client-side only). The RSA key regenerates per startup (KD-4). **`POST /students` and `POST /instructors` are `permitAll()` at the security-filter level, but registration is gated in the service layer**: both paths call `validateUserInvitation`, which rejects any registration lacking a course-admin-issued, single-use invitation whose email, token, role, course, and section all match (invitations are created via UC-STU-1 / UC-INS-1). So account creation is **not** open self-registration — a stranger cannot self-provision, and an instructor account requires an *instructor* invitation. The residual concern is defense-in-depth: the only gate is service-layer (there is no filter-chain authorization rule), and the endpoint is unauthenticated and unthrottled (rate limiting is [TD-6](#risks-and-technical-debt)).

#### **Authorization**

- **Role hierarchy** `admin > instructor > student` (`RoleHierarchyImpl`); method security enabled (`@EnableMethodSecurity`).
- **Two layers:** coarse URL rules in `SecurityConfiguration` (`hasAuthority`, `.authenticated()`) **plus** fine-grained `.access(…AuthorizationManager)` enforcing **ownership** (the user created the resource) or **membership** (the user belongs to the same course/section/team). The logic lives in per-domain `*SecurityService` classes; the `*AuthorizationManager` is a thin URL-to-logic wrapper.
- **Effect:** a student reaches only their own team's data — all RAM artifacts are gated by `teamMembership`/`teamOwnership`; an instructor only their assigned sections. This is the architectural realization of least privilege (QS-1).
- **Client-side role checks are by convention, not by hierarchy.** The `RoleHierarchyImpl` expansion applies **server-side only**. The JWT's `authorities` claim is the user's *literal* `roles` string, and the SPA router guard (`router/guards.ts` `checkPermissions`) does a **flat membership test** against it — it does **not** expand `admin ⇒ instructor ⇒ student`. Client-side gating therefore relies on the convention that **every account's `roles` string already lists each level it should satisfy** (admins are seeded as `"admin instructor"`). **Invariant to uphold:** any admin account must be provisioned with `roles` that include `instructor` (and `student` where student-only routes must be reachable) — an admin created as `"admin"` alone would be authorized server-side yet **blocked from `instructor` routes in the SPA**, a silent client/server mismatch. The alternative (mirror the hierarchy in the client guard) is tracked as OI-38.

#### **Data protection & FERPA**

- **Records handled:** WARs (student contributions), peer evaluations (sensitive peer judgments), scores/feedback, requirements artifacts, identity (names/emails).
- **Least privilege:** the ownership/membership model above — the primary FERPA control.
- **Accountability:** JPA auditing (`PeerEvaluationUserAuditorAware`) stamps created/modified-by + timestamps.
- **Secrets:** `staging`/`prod` load secrets from **Azure Key Vault**; none in source.
- **Encryption:** in transit via Azure-edge TLS; at rest via Azure-managed database encryption — both platform-provided, not app-configured.
- **Known gaps (FERPA obligations not yet designed):** no **retention / deletion / end-of-course purge** policy and no student data **access/correction** workflow (structural cascade deletes exist but are not a retention policy); no documented audit-log retention or data-minimization review. **Wide-open CORS** (`allowedOrigins("*")`) — low risk given bearer-token (non-cookie) auth and same-origin prod serving, but should be allowlisted to the SPA origin. No **rate limiting** on auth endpoints (brute-force exposure; `/users/exists/{email}` also enables email enumeration).

#### **Threat model (scope)**

- **In scope (addressed):** cross-team / cross-section data access (AuthorizationManagers), credential theft (BCrypt-12, short-lived JWT), unauthorized role escalation (role hierarchy + ownership).
- **Out of scope / accepted at current scale:** DDoS and rate-limiting, multi-tenant isolation (single-tenant by design), advanced persistent threats — revisited if scale or deployment model changes.

### *Data architecture*

> The platform's persistence strategy and the decisions behind it — strategy and pointers, not a table-by-table schema (the conceptual model is owned by the requirements specs' domain models; the code owns the physical detail). RAM's requirements-graph persistence is a module-level concern owned by the RAM module (see [RAM components](#ram-components)).

#### **Store & schema**

- One relational store: a single **MySQL 8** schema shared by both modules (KD-2, KD-3). Foundation and performance-tracking tables (WARs, evaluations, courses/sections/teams, users) and RAM tables (documents, artifacts, links, comments) live side by side — one backup, one migration history, one FERPA surface. The one store kept *outside* MySQL is **uploaded project source material**, whose large binaries live in Azure Blob Storage (see [Binary content & file storage](#binary-content--file-storage)).
- ORM is **JPA/Hibernate**; entities use IDENTITY-generated primary keys (`@GeneratedValue(strategy = IDENTITY)`), no Lombok.

#### **Domain & aggregate model**

- `Course` is the aggregate root; ownership cascades downward (`Course` → `Criterion`/`Rubric`/`Section` → `Team` → …, `CascadeType.ALL`) — see the domain model in [`backend/CLAUDE.md`](../../backend/CLAUDE.md) and the requirements specs' domain model. `Activity` and `PeerEvaluation` are independent entities referencing `Student`/`Team`.
- `Student` and `Instructor` extend the abstract `@Entity` `PeerEvaluationUser`, mapped with JPA **single-table inheritance** (the default) — one users table, the shared auth subject for both modules.
- RAM entities (documents, document sections, requirement artifacts, artifact links, use cases, glossary, comments) are **scoped to a Team**; their physical mapping (the artifact table, the typed edge/link table, per-team key sequences) is the RAM module's data architecture (see [RAM components](#ram-components)).

#### **Transactions & consistency**

- The **transaction boundary is the service method** — services are `@Service @Transactional`, so a controller call commits or rolls back as a unit.
- **Concurrency:** core entities rely on transactional consistency without row versioning. RAM, which has concurrent multi-author editing, adds **optimistic locking** (`@Version`) on its mutable content aggregates (`RequirementDocument`, `DocumentSection`, `UseCase`, `ArtifactKeySequence`) to prevent lost updates — and above that, the **pessimistic section-level locks** (`DocumentSectionLock`/`UseCaseLock`, whose own rows are likewise `@Version`-guarded) that serialize human editing (KD-6). The `@Version` on `ArtifactKeySequence` keeps per-team key generation (`UC-1`, `FR-1`, …) collision-free under concurrency.

#### **Schema management**

- Schema ships as **Flyway** migrations (`db/migration/V*.sql`) in `staging`/`prod` (`ddl-auto: none`); `dev` uses `ddl-auto: create` + `DataInitializer` seed data (the *Architectural conventions* above). Schema changes are versioned migrations applied at deploy time.

#### **Binary content & file storage**

- Beyond the relational graph, RAM accepts **uploaded files** — a team's *project source material* (PDF/PPTX, allowlisted file types, ≤ 25 MB per file) imported as input for the AI assistants (FR-IMP-1/2, SI-4, UC-AI-1). For each accepted upload the module stores **the file's bytes** and the **server-side-extracted text** used as assistant context.
- **Storage location:** the large binary files live in **Azure Blob Storage**; MySQL holds only a **reference** (blob path/URL + metadata) and the **server-side-extracted text**. Large media don't belong in the relational store — keeping bytes out of MySQL keeps the DB small and backups/migrations fast. The cost is a **second managed store**: the FERPA surface now spans MySQL **and** the Blob container (both Azure-managed and encrypted at rest, under the one single-tenant trust boundary). The `export` component writes and reads file bytes through the Blob SDK over HTTPS and persists the reference + text over JDBC.
- **Text extraction** runs **server-side** (a parsing step, e.g. Apache Tika / PDFBox + Apache POI) and reports incomplete extraction for image-only or scanned files (FR-IMP-2). The browser never parses files; uploads are multipart to the REST API, which streams the bytes to Blob Storage and persists the reference + extracted text in MySQL.

#### **Document export**

- Export (UC-EXP-1/2, FR-EXP-1/2, SI-3) renders a document — or a **bundle** of all of a team's documents — to **PDF, DOCX, or Markdown** from the stored section content, preserving the template-defined structure (table of contents, heading hierarchy, numbering, formatting). Rendering is a **server-side** step in the `export` component; the specific rendering toolchain/library is an open decision settled at `/design` of the EXP area. Export reads existing data only — it adds no persistent state.

#### **Retention**

- No data **retention / deletion / archival** policy is implemented today (structural cascade deletes are not a retention policy) — a known FERPA gap tracked in [Security & Compliance](#security--compliance) and Risks and Technical Debt.

### *Observability & operations*

> How the running platform is monitored — metrics, tracing, health. Telemetry is wired via Spring Boot **Actuator** + **Micrometer**; the collectors and dashboards run in Docker for local development.

#### **Metrics**

- Actuator + Micrometer expose JVM / HTTP / application metrics; `micrometer-registry-prometheus` publishes them at `/actuator/prometheus` for **Prometheus** to scrape, visualized in **Grafana** (both via `docker-compose`, local dev).

#### **Tracing**

- Distributed tracing via **Micrometer Tracing (Brave)** + `zipkin-reporter-brave`, exported to **Zipkin**; sampling probability **0.1** (10%).

#### **Health & info**

- Actuator `health` with **liveness/readiness probes** enabled, `info` (build/git/java/os), runtime `loggers`, `httpexchanges`, `mappings`, `metrics`.

#### **Logging**

- Spring Boot default (Logback) to stdout; no structured or aggregated log store.

#### **Operations**

- One container on one Azure Web App; releases flow through the CI pipeline to a staging slot (see [Deployment View](#deployment-view)), with Flyway migrations applied at deploy.

#### **Known gaps / operational risks**

- **CRITICAL ([TD-1](#risks-and-technical-debt), P0) — `/actuator/**` is publicly exposed and unauthenticated.** It falls outside the `/api/v1` security rules and hits `anyRequest().permitAll()`; the exposed set includes `env`/`configprops` (`show-values: always`) and `heapdump`, so **environment variables, resolved secrets (DB credentials, Key-Vault values), and full memory dumps are reachable without authentication in production**. Must be locked down — secure `/actuator/**` (admin-only), drop `env`/`heapdump`/`configprops` from web exposure, or bind management to a private port. See [Security & Compliance](#security--compliance).
- **No production observability backend** — Prometheus/Grafana/Zipkin run only in local dev (`docker-compose`); production telemetry is **not collected** anywhere yet. A prod path (e.g. Azure Monitor / Application Insights / Managed Grafana scraping Actuator) is an open item.
- No **alerting** rules or **SLOs**, and no centralized log aggregation.

## **Architecture Decisions**

> The architecturally significant decisions and their rationale (context → decision → consequences, including the alternative rejected). Status is *Accepted* unless noted.

**KD-1 — Single deployable (modular monolith).** *Accepted.*
- **Context:** Instructor-scale deployment, no dedicated ops team; delivery speed and operational simplicity matter more than scaling parts independently.
- **Decision:** Build the Vue SPA into the Spring Boot jar (served from `static/`), ship one Docker image to one Azure Web App — API + SPA in one process.
- **Consequences:** Simplest possible deploy/run (Quality Goal #4); one artifact, one pipeline. *Rejected* microservices / separate SPA hosting — network + ops complexity unjustified at this scale. *Trade-off:* the app scales only as a whole.

**KD-2 — RAM as a module inside the platform.** *Accepted.*
- **Context:** RAM began as a separate project but needs the same course/section/team/student/auth/email infrastructure Project Pulse already had.
- **Decision:** Merge RAM in as `ram/*` bounded contexts on the shared base, not a separate system.
- **Consequences:** Reuses identity, RBAC, org model, email; one deployment; uniform conventions. *Rejected* a standalone RAM service — would duplicate the org/auth model and add cross-service integration. *Trade-off:* RAM's lifecycle is coupled to the platform's.

**KD-3 — Relational DB for the requirements graph.** *Accepted.*
- **Context:** RAM's data is a graph (artifacts + typed links + traceability), which hints at a graph DB — but the platform already runs MySQL with relational tooling/ops.
- **Decision:** Store the graph relationally (artifacts as rows, links as an edge table) in the existing DB.
- **Consequences:** The requirements graph is one relational datastore — one backup/migration/FERPA surface — and reuses JPA + conventions. *Rejected* Neo4j/graph DB — a second datastore and new ops, unjustified at typical per-team graph size. *Trade-off:* deep traversals are SQL joins / recursive queries, not native graph ops. (The graph is wholly relational; the lone exception to the single-store picture is uploaded project source material, whose large binaries live in Azure Blob Storage — see [Data architecture](#data-architecture).)

**KD-4 — Self-issued, stateless JWT (RSA keypair generated at startup).** *Accepted; key handling incidental — revisit.*
- **Context:** Wanted stateless auth (no server-side session store); no external identity provider in scope.
- **Decision:** Self-issue and verify JWTs rather than use sessions or an external IdP. The current implementation generates the RSA keypair at application startup.
- **Consequences:** No session store; simple. *Rejected* external IdP / institutional SSO — beyond integration cost, institutional SSO onboarding is impractical at this scale (the institution's IT will not provision a relying-party integration for a course tool), so the platform authenticates users itself; the SRS's auth requirements (FR-SEC-1, SI-2.1, CO-4) accordingly delegate to *this* mechanism, not to an external IdP. *Rejected* sessions (server state). **Known limitation (incidental, not by design):** because the keypair is generated per startup and not persisted, every restart/redeploy invalidates all live tokens (users re-login) and a second instance can't verify the first's tokens — effectively capping the app at one instance. Externalizing/persisting the keys would lift this. (Drives the Scalability "accepted" trade-off and QS-6; revisit when multi-instance is needed.)

**KD-5 — No Lombok / no MapStruct (pedagogical).** *Accepted.*
- **Context:** The codebase is read and extended by students learning Spring/Java; annotation-processor "magic" can obscure what the code actually does.
- **Decision:** Explicit getters/setters/constructors and explicit `Converter<S,T>` beans — no Lombok, no MapStruct.
- **Consequences:** Fully explicit, debuggable code with no build-time codegen, so students see exactly what runs — a deliberate teaching choice. *Rejected* Lombok/MapStruct — less boilerplate but hidden behavior and extra tooling to learn. *Trade-off:* more verbose, hand-written conversion code.

**KD-6 — Pessimistic section-level locking for collaborative editing.** *Accepted.*
- **Context:** Teammates edit the same requirement document concurrently; lost updates on authored content are unacceptable, and a predictable model beats complex merge.
- **Decision:** Lock at document-section (and use-case) granularity — one editor holds a section; others are blocked.
- **Consequences:** No lost updates, simple mental model, fine-grained enough for parallel work on different sections. *Rejected* optimistic concurrency / OT / CRDT real-time co-editing — far more complex; real-time presence/broadcast (UC-COL-1) is **deferred** (not in the current release) and would be a *future layer on top*, not a replacement — until then there is no real-time push channel in the topology, and the related targets (PER-1 presence-propagation, ROB-3) are out of scope. *Trade-off:* two people can't edit the same section at once.

## **Quality Requirements**

> Refines the [Quality Goals](#quality-goals) into a prioritized quality tree and concrete, measurable scenarios. Each scenario is ATAM-style (source · stimulus · environment → response → **measure**); the measures are the testable targets the architecture must hold. Targets marked 🔢 are placeholders (`TBD`) to be set by the team.

### *Quality tree*

- **Security** *(High)* — confidentiality of student records; authorization correctness; auditability → QS-1, QS-2
- **Maintainability** *(High)* — modifiability (add a bounded context), convention consistency, testability → QS-3
- **Usability** *(High)* — low-friction submission, error clarity, no lost work → QS-4. **Accessibility** (WCAG 2.1 AA — keyboard operability, contrast, screen-reader support; required by the SRS's USE-1/UI-2, addressing risk RI-6) is **in scope but not yet architecturally addressed** — deferred, tracked as [TD-11](#risks-and-technical-debt); it is not yet a committed quality goal with a measurable scenario.
- **Reliability** *(High)* — availability under external-dependency failure, data integrity, predictable deploys → QS-5, QS-6
- **Performance efficiency** *(Medium)* — responsive interactions at course scale → QS-7
- **Scalability / portability** *(Low — accepted)* — single-instance topology; horizontal scaling out of scope at current scale (see KD-4 for the JWT-key limitation that currently enforces it)

### *Quality scenarios*

| ID | Goal | Scenario (source · stimulus · environment) | Response | Response measure |
|---|---|---|---|---|
| QS-1 | Security | An authenticated student requests another team's WAR/peer-eval via the API · normal op | Denied at the `AuthorizationManager` | 100% of cross-team/owner-mismatch attempts return `403`; no record fields leak; attempt is auditable |
| QS-2 | Security | An unauthenticated client calls a protected `/api/v1` endpoint · normal op | Rejected before controller logic | `401` returned; no business logic executes; covered by integration tests |
| QS-3 | Maintainability *(change)* | A contributor adds a new bounded context · development | Added as a vertical slice using standard conventions, no edits to existing slices | Zero changes to other bounded-context packages; new endpoints return the `Result` envelope and pass convention checks; delivered in ≤ `TBD` person-days 🔢 |
| QS-4 | Usability | A student is mid-edit in a RAM document section · normal op | Edits autosave; the section is locked against collisions | Autosave at least every 10 s and immediately on navigate-away (PER-2); ≤ 10 s of edits lost on crash/disconnect (ROB-1); a second editor is blocked with a clear message |
| QS-5 | Reliability *(availability)* | The LLM service times out or is down · degraded | AI features degrade gracefully; authoring/saving unaffected | Authoring + save unaffected; AI shows a response or a clear working/timeout indication within 15 s (PER-4) and offers retry; no data loss |
| QS-6 | Reliability | A new release is deployed · deploy-time | Schema migrates; one container serves API + SPA | Flyway migrations apply cleanly; staging-slot smoke check passes before swap; overall availability ≥ 99% per academic term excluding scheduled maintenance (AVL-1/FR-PERF-2); **note:** new RSA key invalidates live JWTs → users re-login (see KD-4) |
| QS-7 | Performance | A student loads a team's requirements graph and runs ReqLint at course scale (~75 total users, ≤ 100 concurrent editors — SCA-1; ~`TBD` artifacts) · normal op | Page and validation respond within target | ReqLint returns within 3 s for 95% of runs on a single document (PER-3); p95 graph-load API response < `TBD` ms at `TBD` artifacts 🔢 |

## **Risks and Technical Debt**

> The consolidated, honest backlog of architecturally significant **risks** (uncertain/external) and **technical debt** (known deficiencies the architecture currently carries) surfaced across this document, prioritized **P0** (fix now) to **P3** (low). Each row cross-references the section that describes it. All items are open unless noted. **TD-1 is an active production exposure and should be fixed first.**

| ID | Pri | Type | Item & impact | Mitigation / fix | Refs |
|---|---|---|---|---|---|
| TD-1 | **P0** | Debt (config) | `/actuator/**` is public & unauthenticated; `env`/`configprops` (`show-values: always`) + `heapdump` leak secrets (DB creds, Key-Vault values) and memory/PII in production | Secure `/actuator/**` (admin-only) + trim the web-exposed set, or bind management to a private port | Observability, Security |
| TD-2 | P1 | Debt (FERPA) | No retention / deletion / end-of-course purge, and no student data access/correction workflow for education records | Define a retention schedule + deletion/anonymization + data-subject workflows | Security, Data |
| TD-3 | P3 | Debt (security, defense-in-depth) | Registration endpoints (`POST /students`, `/instructors`) are `permitAll` at the filter; account creation is gated **only** in the service layer by `validateUserInvitation` (course-admin-issued single-use invitation matching email/token/role/course/section). Not open registration — but the sole gate is service-layer and the endpoint is unauthenticated/unthrottled | Add a filter-chain rule and/or rate limiting (TD-6); keep the invitation gate | Security |
| TD-4 | P2 | Debt (security) | No JWT revocation/refresh; a leaked token is valid its full 2-hour life (logout is client-side only) | Short-lived access + refresh tokens, or a revocation list / token version | Security |
| TD-5 | P2 | Debt (security) | Wildcard CORS (`allowedOrigins("*")`) — low risk given bearer (non-cookie) auth, but not best practice | Allowlist the SPA origin per profile | Security |
| TD-6 | P2 | Debt (security) | No rate limiting on auth endpoints; `/users/exists/{email}` enables email enumeration | Add throttling/rate limits; restrict the exists endpoint | Security |
| TD-7 | P2 | Debt (scaling) | Per-startup RSA key: redeploy forces re-login and blocks multi-instance token verification | Externalize/persist the keypair (shared JWKS) | KD-4, Deployment |
| TD-8 | P2 | Debt (scaling) | `@Scheduled` reminder job fires on every instance → duplicate emails if scaled out | Single-execution coordination (ShedLock / leader election / dedicated scheduler) | Deployment |
| TD-9 | P2 | Debt (ops) | No production observability backend — the Prometheus/Grafana/Zipkin stack is dev-only | Wire prod telemetry (Azure Monitor / App Insights / Managed Grafana) | Observability |
| TD-11 | P2 | Debt (accessibility) | No accessibility architecture: WCAG 2.1 AA (keyboard operability, contrast, screen-reader support) is required by USE-1/UI-2 (risk RI-6) but is not reflected in component choices or verified anywhere | Set an accessibility baseline (component-library a11y audit, keyboard-nav + focus management) and add automated checks (e.g. axe) to CI; verify against WCAG 2.1 AA | Quality Requirements; SRS USE-1/UI-2 |
| RISK-1 | P2 | Risk | Single instance = single point of failure; downtime on failure/restart | Move to multi-instance once TD-7/TD-8 clear; rely on staging-slot swap meanwhile | Deployment |
| RISK-2 | P3 | Risk | External LLM dependency (availability, cost, latency, vendor change) | Timeouts + graceful degradation (QS-5); a provider abstraction | Runtime, QS-5 |
| TD-10 | P3 | Debt (ops) | No alerting/SLOs and no centralized log aggregation | Define SLOs + alerts + ship logs to an aggregator | Observability |

## **Glossary**

The domain vocabulary is defined in the [project glossary](../requirements/project-glossary.md). Architecture terms used in this document:

- **Architecture-of-record** — the single canonical architecture description this doc *is*; changes only when the platform architecture changes, not per feature.
- **Bounded context / vertical slice** — one DDD domain per package owning its entity → repository → service → controller → DTO/converter stack.
- **`Result` envelope** — the standard response wrapper (`flag`/`code`/`message`/`data`) every controller returns.
- **Ownership vs membership** — the two fine-grained authorization checks: *ownership* = the user created the resource; *membership* = the user belongs to the same course/section/team.
- **Module / platform** — RAM is a *module* inside the Project Pulse *platform*; the platform owns the conventions, RAM cites them.
