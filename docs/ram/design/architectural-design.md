# Architectural Design

> Scope: the host architecture RAM is constrained to, its operating environment, design/implementation constraints, the architecture-level assumptions and dependencies, and deployment.
> See: ../requirements/software-requirements-specification.md (which references this doc for architecture and deployment), ../requirements/vision-and-scope.md

This is a **cross-cutting** design doc, not a per-UC-area one. It is the design-of-record for how the RAM module sits inside the Project Pulse host platform — the system context and container views, the operating environment, the design and implementation constraints, the architecture-level assumptions and dependencies, and how RAM is deployed. The per-area design docs (`doc.md`, `art.md`, …) cite the views here (especially the **Container Diagram**) rather than redrawing them.

The architecture-level requirement artifacts in this doc — `OE-*` (operating environment), `CO-*` (constraints), and the architecture-level `AS-*` / `DE-*` (assumptions and dependencies) — keep their stable IDs and are cited by ID from the SRS (Data Requirements, External Interface Requirements, Quality Attributes) and traceability. The C4 diagrams depict architecture RAM is **given** (a module inside a fixed host, per CO-1 / OE-4), not architecture decided here.

## System Context Diagram

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

## Container Diagram

```mermaid
C4Container
    title Container Diagram for Project Pulse

    Person(student, "Senior Design Student", "Enrolled in the course")

    Container_Boundary(pulse, "Project Pulse") {
        Container(spa, "SPA", "Vue.js", "Course-management UI + RAM authoring views (graph, editor, ReqLint, AI panel)")
        Container(api, "REST API Application", "Java / Spring Boot", "Course-management & RAM APIs (graph, ReqLint, AI proxy)")
        ContainerDb(db, "Database", "Relational DB", "WARs, peer evals, and RAM artifacts/links/documents")
    }

    System_Ext(gmail, "Gmail", "Email system")
    System_Ext(llm, "LLM Service", "AI-assisted requirement review")

    Rel_R(student, spa, "Uses", "HTTPS")
    Rel_D(spa, api, "API calls", "JSON/HTTPS")
    Rel_D(api, db, "Reads & writes", "JDBC")
    Rel_R(api, gmail, "Sends email", "SMTP")
    Rel_R(api, llm, "Requests AI review", "HTTPS")
```

The Level 2: Container Diagram for the Project Pulse system provides a detailed view of its internal architecture, illustrating how the system components interact. The system is composed of three key containers — the **SPA (Single Page Application)**, the **REST API Application**, and the **Database** — supported by integration with the **Gmail System** for email communication and an external **LLM Service** (e.g., OpenAI) for AI-assisted requirement review. The **SPA**, built with Vue.js, is delivered to users' browsers and provides the interface for both the course-management workflows (submitting WARs and peer evaluations) and the RAM module's requirements authoring views: graph navigation, document editing, the ReqLint validation sidebar, and the AI assistant panel. The **REST API Application**, implemented using Java and Spring Boot, delivers the SPA, processes REST API calls, and manages interactions with the **Database**; for the RAM module it exposes endpoints for the requirements graph, ReqLint validation, and an AI proxy to the LLM service. The **Database**, a relational database, stores course-management data (WARs and peer evaluation submissions) alongside the RAM module's requirement artifacts, links, documents, and document sections, with CRUD operations executed through the REST API. The REST API integrates with the **Gmail System** over SMTP to send automated notifications and with the external **LLM Service** to support AI-assisted review. The steps below trace the WAR submission flow as a representative example:

1. Visit the Project Pulse Website: The Senior Design student begins by accessing the Project Pulse system through their browser at the URL https://projectpulse.team.
2. Deliver the SPA to the student's Browser: The REST API Application (built using Java and Spring Boot) serves the Single Page Application (SPA, built with Vue.js) to the student's browser. This provides the user interface that students interact with.
3. Submit WARs and Peer Evaluations: The student uses the SPA to complete and submit Weekly Activity Reports (WARs) and peer evaluations through the interface.
4. Make REST API Calls to the Backend: The SPA communicates with the REST API Application by making REST API calls to process and handle the submissions from the student. These calls allow the backend to manage the application's logic and facilitate data processing.
5. CRUD Operations with the Database: The REST API Application performs CRUD (Create, Read, Update, Delete) operations on the Database, which is a relational database. The database securely stores the submitted WARs and peer evaluations.
6. Send Emails via SMTP: The REST API Application interacts with the Gmail system using SMTP to send automated email notifications (e.g., reminders, submission confirmations) to the student or instructor, as necessary.
7. Receive Emails: Finally, the student (or instructor) receives email notifications generated by the Gmail system, completing the interaction loop.

This sequence of actions outlines how the Project Pulse system components collaborate to facilitate functionality for the student while ensuring efficient data management and communication. RAM-specific flows (graph navigation, document editing, ReqLint validation, and AI-assisted review) follow the same SPA → REST API → Database path, with the REST API additionally proxying requests to the external LLM service.

## Operating Environment

OE-1: RAM operates as a module of the Project Pulse web application and shall run in the current released versions of Google Chrome, Mozilla Firefox, Microsoft Edge, and Apple Safari.

OE-2: The Project Pulse server shall run the REST API as a Java/Spring Boot application on a supported Java Virtual Machine, serve the Vue.js single-page application to clients, and persist data in a relational database.

OE-3: Users shall access RAM over HTTPS from the public internet, requiring no client software beyond a web browser.

OE-4: RAM shall be deployed within the existing Project Pulse deployment and coexist with the existing WAR and peer-evaluation functionality, sharing the same single-page application, REST API, and database.

OE-5: RAM shall reach the external LLM service over HTTPS and the Gmail system over SMTP for AI-assisted review and email notifications, respectively.

## Design and Implementation Constraints

CO-1: RAM shall be implemented as a module within the existing Project Pulse codebase, reusing its Vue.js single-page application, Java/Spring Boot REST API, and relational database rather than introducing a separate system.

CO-2: The client shall be implemented in Vue.js and the backend in Java using the Spring Boot framework.

CO-3: Requirement artifacts, links, documents, and document sections shall be persisted in the existing Project Pulse relational database.

CO-4: User authentication shall be delegated to the host platform's institutional Single Sign-On (SSO); RAM shall not implement a separate login mechanism.

CO-5: RAM shall comply with FERPA when storing and transmitting student educational records.

CO-6: All calls to the external LLM service shall be routed through the REST API's AI proxy so that service credentials remain server-side and are never exposed to the browser.

CO-7: Email notifications shall be sent through the existing Gmail SMTP integration.

## Assumptions and Dependencies

_Assumptions and dependencies are graph artifacts with **team-wide** key sequences (`AS-*`, `DE-*`) that are unique within a team (BR-5) — documents are views over one shared graph, so the keys do not restart per document. The business-level assumptions `AS-1`…`AS-5` are in Vision and Scope's Business Assumptions and Dependencies; the architecture-level assumptions below continue that same `AS-*` sequence._

AS-6: Users have a supported web browser and a reliable internet connection.

AS-7: The external LLM service (e.g., OpenAI) remains available and its API contract stays stable for the integration RAM relies on.

AS-8: Project Pulse's existing course, course section, team, and user data is accurate and current; RAM reuses this data rather than maintaining its own copy.

DE-1: RAM depends on Project Pulse for authentication and Single Sign-On, the course/course section/team data model, and the course admin, instructor, and student roles.

DE-2: AI-assisted requirement review depends on the external LLM service; if it is unavailable, the AI features are unavailable while the rest of RAM continues to operate.

DE-3: Email notifications depend on the Gmail SMTP integration provided by Project Pulse.

## Deployment

RAM is a module within the existing Project Pulse application (CO-1, INT-1), so it is deployed, operated, and maintained as part of Project Pulse rather than as a separate system — the same Vue.js single-page application, Spring Boot REST API, and relational database, through the same pipeline and environments. The target deployment environment and operational specifics — Microsoft Azure App Service, GitHub Actions CI/CD, the external OpenAI LLM integration, and load testing for peak usage — are given in Vision and Scope's Deployment Considerations and are not repeated here; the runtime integrations RAM relies on are specified in the Operating Environment above and in the SRS's External Interface Requirements section. Schema changes for the requirements graph are delivered as versioned database migrations applied during deployment.
