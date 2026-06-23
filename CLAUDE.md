# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Project Pulse is a web application for managing senior design / capstone course projects. Students submit weekly activity reports and peer evaluations; instructors monitor progress via dashboards. It also includes a Requirements Authoring & Management (RAM) module that lets student teams define software requirements before coding — project glossary, vision and scope, use cases, business rules, software requirements specifications, and traceability.

RAM functionality is built **spec-first**: its requirements live as Markdown under `docs/requirements/` and drive implementation through the `/design` → `/implement` workflow. See **Spec-driven development** below before adding or changing RAM features.

The overarching development methodology these workflows instantiate — spec-driven and agent-assisted, with a *breadth-complete, depth-shallow* architecture validated by a thin vertical slice before fanning out per use case — is written up in [`docs/methodology.md`](docs/methodology.md).

## Starting the Project

All three services must be running for local development. Run them in this order:

1. **Docker services** (MySQL on 3306, Mailpit SMTP on 1025 / web UI on 8025):
   ```
   docker-compose up -d
   ```
2. **Backend** (Spring Boot on port 80, dev profile with seed data):
   ```
   cd backend
   mvnw.cmd spring-boot:run       # Windows
   ./mvnw spring-boot:run          # macOS/Linux
   ```
3. **Frontend** (Vite dev server on http://localhost:5173):
   ```
   cd frontend
   npm install
   npm run dev
   ```

The app is ready at http://localhost:5173. Mailpit UI is at http://localhost:8025.

## Stopping the Project

1. **Backend & Frontend:** Kill processes by port to avoid affecting other Java/Node processes:
   ```
   # Windows (PowerShell) — backend on port 80, frontend on port 5173
   (Get-NetTCPConnection -LocalPort 80 -ErrorAction SilentlyContinue).OwningProcess | ForEach-Object { Stop-Process -Id $_ -Force }
   (Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue).OwningProcess | ForEach-Object { Stop-Process -Id $_ -Force }
   
   # macOS/Linux
   lsof -ti:80 | xargs kill
   lsof -ti:5173 | xargs kill
   ```
2. **Docker services:**
   ```
   docker-compose down
   ```

## Development Commands

### Backend (Spring Boot 4 + Maven)
Use `mvnw.cmd` on Windows, `./mvnw` on macOS/Linux:
```
cd backend
mvnw.cmd spring-boot:run                    # Run with dev profile (port 80)
mvnw.cmd test                               # Run all tests
mvnw.cmd test -Dtest=ActivityServiceTest     # Run a single test class
mvnw.cmd test -Dtest="ActivityServiceTest#testFindActivityByIdSuccess"  # Single method
mvnw.cmd package                            # Build jar (runs tests)
mvnw.cmd package -DskipTests                # Build jar without tests
```

### Frontend (Vue 3 + Vite + TypeScript)
```
cd frontend
npm run dev          # Dev server on http://localhost:5173
npm run build        # Type-check + production build
npm run build-only   # Production build without type-check
npm run type-check   # vue-tsc type checking
npm run lint         # ESLint with auto-fix
npm run format       # Prettier
npm run cy:open      # Cypress E2E tests
```

### Dev Credentials
- Student: `j.smith@abc.edu` / `123456`
- Instructor (admin): `b.wei@abc.edu` / `123456`

## Architecture

> **Canonical architecture-of-record:** [`docs/design/architectural-design.md`](docs/design/architectural-design.md) — the single whole-product architecture: the C4 context/container views, the foundation/performance-tracking and RAM component views, the binding conventions, the cross-cutting subsystems, and deployment. When the architecture changes, update *that* doc; the summary below is just orientation for working in the code. The architecture-of-record follows the **arc42** template (with **C4** for the context/building-block views); the requirements specs follow **Wiegers & Beatty** — see [`docs/methodology.md`](docs/methodology.md).

### Monorepo Layout
- `backend/` — Spring Boot 4.0 Maven project (Java 21)
- `frontend/` — Vue 3 + Vite + TypeScript SPA
- `docker-compose.yml` — MySQL 8, Mailpit, Prometheus, Grafana, Zipkin

### Deployment
One Azure deployment for the whole platform (frontend bundled into the Spring Boot jar, served alongside the API). Canonical detail — pipeline stages, environments, SPA serving — is in the architecture-of-record's [Deployment](docs/design/architectural-design.md#deployment-view) section; see also **CI** at the end of this file.

### Backend Architecture

**Domain-Driven Design (DDD)** — each domain (bounded context) lives in its own package under `team.projectpulse.*` (e.g., `activity`, `evaluation`, `course`, `student`, `team`, `section`, `rubric`, `instructor`) and owns its full vertical slice:
- Entity, Repository, Service, Controller, DTOs, Converters, and a `*SecurityService` (or `*Specs` for dynamic queries)

**Key cross-cutting packages:**
- `system/` — `Result` (standard API response envelope with `flag`, `code`, `message`, `data`), `StatusCode` constants, `ExceptionHandlerAdvice` (global `@RestControllerAdvice`), `DataInitializer` (dev-profile seed data), `EmailService`, clock configs
- `security/` — JWT-based auth (RSA key pair generated at startup), `SecurityConfiguration` defines URL-level authorization rules, `authorizationmanagers/` package has fine-grained ownership/membership `AuthorizationManager` implementations
- `user/` — Shared `PeerEvaluationUser` base class, password reset, user invitation flows

**RAM domain** (`ram/`): Requirements Authoring & Management — originally a separate project, merged into Project Pulse to reuse the existing course/section/team/student infrastructure. Sub-packages: `document/` (requirement documents with section-level pessimistic locking), `requirement/` (artifacts, traceability links), `usecase/`, `glossary/`, `collaboration/` (comment threads)

**Patterns:** the binding conventions every package follows — `/api/v1` endpoints returning the `Result` envelope, DDD vertical slice with `Converter<S,T>` DTOs (no Lombok/MapStruct), JWT + `AuthorizationManager` auth (`admin > instructor > student`), Flyway migrations — are stated normatively in the architecture-of-record's [Architectural Conventions](docs/design/architectural-design.md#architectural-conventions). Follow them; don't restate them here.

**Spring profiles:** `dev` (default, local MySQL + Mailpit), `staging`, `prod` (Azure Key Vault for secrets)

### Frontend Architecture

- **Router:** `router/routes.ts` defines all routes with `meta` flags (`requiresAuth`, `visitorOnly`, `requiresPermissions`). Guards in `router/guards.ts` enforce auth/role checks via JWT decoding.
- **State:** Pinia stores in `stores/` — `token.ts` (JWT, persisted), `userInfo.ts` (decoded user details + roles), `menuRoute.ts`, `settings.ts`
- **API layer:** `apis/<feature>/index.ts` + `types.ts` per domain. All use a shared Axios instance (`utils/request.ts`) that adds Bearer token, handles 401 redirect to login, and unwraps `response.data` automatically.
- **UI framework:** Element Plus components, SCSS for custom styles, Chart.js via vue-chartjs, TipTap rich text editor (RAM documents)
- **E2E tests:** Cypress (`frontend/cypress/e2e/`)

### Testing

**Backend tests** are split into:
- Unit tests (`*ServiceTest.java`) — mock repositories with Mockito
- Integration tests (`*IntegrationTest.java`) — use Testcontainers with MySQL, test full controller→DB round-trips with `@SpringBootTest` and `MockMvc`

Both types live under `backend/src/test/java/team/projectpulse/`.

## Spec-driven development

Project Pulse is developed **spec-first**: its requirements are authored as Markdown and are the contract the code implements. This is the standing method for new work and applies most actively to the RAM module (built spec-first from the start). Don't design features ad hoc — start from the spec, build from it, and trace the work back.

### The spec is the source of truth

`docs/` is organized **doctype-first** — one spec set for the whole product (the shared foundation, performance tracking, and RAM), grouped by document type, not by module:

- `docs/requirements/` — the spec (what):
   1. `project-glossary.md` — domain vocabulary; canonical term definitions.
   2. `vision-and-scope.md` — business objectives (BO-*), risks (RI-*), assumptions (AS-*), features.
   3. `use-cases.md` — behavioral specs as use cases with area-prefixed IDs (`UC-WAR-manage-activities`, `UC-GLO-view-glossary`, `UC-DOC-create-use-case`), grouped by area.
   4. `business-rules.md` — cross-cutting policies, constraints, and access rules (BR-*).
   5. `software-requirements-specification.md` — functional requirements (FR-*), domain model, quality attributes.
   - `OPEN-ISSUES.md` — the working backlog (`OI-n`, P0–P3) of gaps still needed to make the spec implementation-ready.
- `docs/design/` — `architectural-design.md` (the one arc42/C4 architecture-of-record for the whole product) plus per-UC-area design docs generated from the spec. The area docs sit *below* the SRS (component/class design, sequence diagrams, API contracts, DB schema) and cite the UC/FRs they realize without restating them.
- `docs/traceability.md` — the spec→code map: one row per use case → FR IDs → design doc → frontend/backend modules → tests → status.
- `docs/guides/` — supporting build guidance that isn't itself a spec doc (e.g., AI implementation notes).
- `docs/product/` — **product material the running product uses at runtime, not a spec of Project Pulse**: shipped default content the product seeds at runtime (e.g., the default cross-document review criteria for the whole-project critique, and the requirement-quality criteria for the per-destination critique, each with its critique-assistant system prompt). Project Pulse *operates with* this to evaluate students' requirements; the spec that *describes* Project Pulse is the rest of `docs/`.
- `docs/CLAUDE.md` — authoring rules for these docs (anchor slugs, ID schemes, cross-doc consistency); it governs edits anywhere under `docs/`.

Use cases are grouped by **area** — a short code that mirrors a backend bounded context: the foundation and performance-tracking areas `RUB`/`SEC`/`TEA`/`STU`/`INS`/`ACC`/`WAR`/`EVA`, then RAM areas `TPL`/`GLO`/`DOC`/`ART`/`LNK`/`VAL`/`COL`/`REV`/`EXP`/`CFG`/`AI` (`docs/CLAUDE.md` enumerates them). The foundation and performance-tracking features were built before this spec set existed, so their use cases are documented retrospectively; RAM use cases drive new implementation.

**Functional requirements.** A **use case is itself a high-level functional requirement** (the SRS's Use Cases section) — its "The system ..." steps + Associated Information are its detailed spec. The SRS's **Non-Use Case Functional Requirements** section holds only the non-use-case, system-level behaviors, with IDs in `FR-<AREA>-<slug>` format (parallel to `UC-<AREA>-<slug>`; `docs/CLAUDE.md` enumerates the area codes). **Business rules** (`BR-*`, in `business-rules.md`) are flat name-based slugs cited by use cases and the SRS. FR/BR/UC IDs are name-based identifier spaces independent of heading position — their slugs are stable handles, so inserting or reordering never renumbers them.

### Spec-driven feature workflow

A feature begins as a use case. The loop:

1. A use case is added or changed in `docs/requirements/use-cases.md` (follow `docs/CLAUDE.md`; run `/spec-build` to resync anchors and cross-references).
2. Run **`/design <UC-ID>`** (`.claude/commands/design.md`) to turn it into an approved **design-of-record** — the area's `docs/design/<area>.md` doc (diagrams + non-obvious decisions). Design is a separately-reviewed stage that **stops before code**.
3. Run **`/implement <UC-ID>`** (`.claude/commands/implement.md`) to build from that design — plan → code → test.
4. The work is recorded back into `docs/traceability.md` (`/design` marks the row `📐 Designed`; `/implement` flips it to the built state).

Treat the use case as the contract:
- The **use case** gives actors, trigger, main success scenario, extensions, and pre/postconditions — *what to build and the flows to test*. Its system-subject steps plus their **Associated Information** are themselves the detailed functional requirement (a use case is a high-level FR); the cross-cutting **non-use-case FRs** (`FR-LOCK-*`, `FR-SAVE-*`, …) it cites are the additional atomic "shall" statements it must honor. Together they are the *acceptance criteria* — don't restate the use-case flow as a new FR, and don't invent missing detail silently.
- **Citation is at the use-case level.** Traceability is one row per UC and tests are tagged to the UC — there is no finer (per-step) handle, so the UC's tests must cover the whole main flow **and every extension**, and a use case must stay small enough that "UC-X passes" is a meaningful statement. The extensions carry the edge-case requirements.
- The **glossary** fixes vocabulary — use the defined term in code identifiers and UI text, never a synonym.
- **The spec is authoritative but not infallible.** When a step is ambiguous, an assumption breaks against the existing code, or requirements contradict — ask a clarifying question or challenge the spec; don't silently comply or silently invent. Fix the spec and re-derive rather than diverging quietly in code. `/design` and `/implement` build this in.

Cross-cutting behavior is already specified, and some is already built — reuse it, don't reinvent per feature:
- **Locking** (`FR-LOCK-*`): section-level pessimistic locking already exists in `ram/document/`. See UC-DOC-edit-document / UC-DOC-edit-use-case.
- **Collaboration** (`FR-COL-*`): comment threads exist in `ram/collaboration/`; real-time presence/broadcast is specified in UC-COL-collaborative-edit (not yet built).
- **Validation** (`FR-VAL-*`, ReqLint): deterministic structural checks. See UC-VAL-run-validation.

When implementing, **extend the existing RAM packages** (`ram/document`, `ram/requirement`, `ram/usecase`, `ram/glossary`, `ram/collaboration`) and the shared course/section/team/auth/email infrastructure — RAM is a module inside this codebase, not a separate system, so don't fork or duplicate the architecture "for RAM." Then map the use case back into `docs/traceability.md` (frontend `apis/` + views + stores, backend `ram/*` controller/service/repository/entity, tests).

### Editing the docs

When editing anything under `docs/`, the rules in `docs/CLAUDE.md` apply (anchor slugs, FR/BR/UC ID schemes, cross-doc terminology). Run **`/spec-build`** to verify and resync.

To catch drift between the specs and the **code** (not just within the docs), run **`/sync-check`** (`.claude/commands/sync-check.md`) — the periodic spec↔code conformance audit that complements `/spec-build` (docs-internal consistency). It reads the business rules / use cases / FRs, checks them against the actual services, controllers, and enums, and records any new divergence as an `OI-n` in `docs/requirements/OPEN-ISSUES.md`. Run it after a batch of code or spec changes, or on a schedule.

## CI

- **PR checks:** `maven-build.yml` runs `mvn package` (builds + tests backend) on PRs to `main`
- **Deploy:** `azure-webapps-deploy.yml` on push to `main` — builds frontend, bundles into Spring Boot jar, Docker image → GHCR → Azure Web App staging slot
