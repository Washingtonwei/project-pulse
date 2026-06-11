# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Project Pulse is a web application for managing senior design / capstone course projects. Students submit weekly activity reports and peer evaluations; instructors monitor progress via dashboards. It also includes a Requirements Authoring & Management (RAM) module that lets student teams define software requirements before coding — project glossary, vision and scope, use cases, business rules, software requirements specifications, and traceability.

RAM functionality is built **spec-first**: its requirements live as Markdown under `docs/ram/` and drive implementation through the `/feature` workflow. See **Spec-driven RAM development** below before adding or changing RAM features.

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

### Monorepo Layout
- `backend/` — Spring Boot 4.0 Maven project (Java 21)
- `frontend/` — Vue 3 + Vite + TypeScript SPA
- `docker-compose.yml` — MySQL 8, Mailpit, Prometheus, Grafana, Zipkin

### Deployment
The CI pipeline (`azure-webapps-deploy.yml`) builds the Vue frontend, copies the dist into `backend/src/main/resources/static/`, then builds the Spring Boot jar and deploys as a Docker container to Azure. In production, the Spring Boot app serves both the API and the SPA. `WebConfig` forwards non-API routes to `index.html` for SPA routing.

### Backend Architecture

**Domain-Driven Design (DDD)** — each domain (bounded context) lives in its own package under `team.projectpulse.*` (e.g., `activity`, `evaluation`, `course`, `student`, `team`, `section`, `rubric`, `instructor`) and owns its full vertical slice:
- Entity, Repository, Service, Controller, DTOs, Converters, and a `*SecurityService` (or `*Specs` for dynamic queries)

**Key cross-cutting packages:**
- `system/` — `Result` (standard API response envelope with `flag`, `code`, `message`, `data`), `StatusCode` constants, `ExceptionHandlerAdvice` (global `@RestControllerAdvice`), `DataInitializer` (dev-profile seed data), `EmailService`, clock configs
- `security/` — JWT-based auth (RSA key pair generated at startup), `SecurityConfiguration` defines URL-level authorization rules, `authorizationmanagers/` package has fine-grained ownership/membership `AuthorizationManager` implementations
- `user/` — Shared `PeerEvaluationUser` base class, password reset, user invitation flows

**RAM domain** (`ram/`): Requirements Authoring & Management — originally a separate project, merged into Project Pulse to reuse the existing course/section/team/student infrastructure. Sub-packages: `document/` (requirement documents with section-level pessimistic locking), `requirement/` (artifacts, traceability links), `usecase/`, `glossary/`, `collaboration/` (comment threads)

**Patterns:**
- All API endpoints are prefixed with `/api/v1` (configured via `api.endpoint.base-url` in `application.yml`)
- All controller methods return `Result` objects (never raw entities)
- Bidirectional DTO conversion using Spring `Converter<S,T>` implementations (not MapStruct/Lombok)
- No Lombok — all entities use explicit getters/setters/constructors
- Role hierarchy: `admin > instructor > student`
- Authorization uses both URL-level rules in `SecurityFilterChain` and custom `AuthorizationManager` beans for ownership/membership checks
- Database migrations via Flyway (`backend/src/main/resources/db/migration/`)
- Dev profile uses `ddl-auto: create` with `DataInitializer`; prod uses Flyway only

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

## Spec-driven RAM development

The RAM module is developed **spec-first**: its requirements are authored as Markdown and are the contract the code implements. Don't design RAM features ad hoc — start from the spec, build from it, and trace the work back.

### The spec is the source of truth

`docs/` is organized **area-first** — each module gets a self-contained subtree holding its full spec→design chain. RAM lives under `docs/ram/`:

- `docs/ram/requirements/` — the spec (what):
   1. `project-glossary.md` — domain vocabulary; canonical term definitions.
   2. `vision-and-scope.md` — business objectives (BO-*), risks (RI-*), assumptions (AS-*), features.
   3. `use-cases.md` — behavioral specs as use cases with area-prefixed IDs (`UC-GLO-1`, `UC-DOC-5`, `UC-AI-3`), grouped by area.
   4. `business-rules.md` — cross-cutting policies, constraints, and access rules (BR-*).
   5. `software-requirements-specification.md` — architecture, functional requirements (FR-*), quality attributes.
- `docs/ram/design/` — design docs generated from the spec, one per UC area. They sit *below* the SRS (component/class design, sequence diagrams, API contracts, DB schema) and cite the UC/FRs they realize without restating them.
- `docs/ram/traceability.md` — the spec→code map: one row per use case → FR IDs → design doc → frontend/backend modules → tests → status.
- `docs/ram/requirements/OPEN-ISSUES.md` — the working backlog (`OI-n`, P0–P3) of gaps still needed to make the spec implementation-ready.
- `docs/ram/guides/` — supporting build guidance that isn't itself a spec doc (e.g., AI implementation notes).
- `docs/ram/product/` — RAM product material: shipped default content the product seeds at runtime (e.g., the default cross-document review criteria + critique-assistant system prompt).
- `docs/ram/CLAUDE.md` — authoring rules for these docs (numbering, TOCs, anchor slugs, ID schemes, cross-doc consistency); it governs edits anywhere under `docs/ram/`.

(A second, non-RAM requirement set is scaffolded as a placeholder under `docs/pulse-core/` and will be authored later with the same shape.)

**Functional requirements.** A **use case is itself a high-level functional requirement** (SRS §5.1) — its "The system ..." steps + Associated Information are its detailed spec. SRS **§5.2** holds only the non-use-case, system-level behaviors, with IDs in `FR-<AREA>-<n>` format (parallel to `UC-<AREA>-<n>`; `docs/ram/CLAUDE.md` enumerates the area codes). **Business rules** (`BR-*`, in `business-rules.md`) are an append-only sequence cited by use cases and the SRS. FR/BR/UC IDs are identifier spaces independent of section numbering — never renumber them.

### Spec-driven feature workflow

A RAM feature begins as a use case. The loop:

1. A use case is added or changed in `docs/ram/requirements/use-cases.md` (follow `docs/ram/CLAUDE.md`; run `/build` to resync TOCs, numbering, and cross-references).
2. Run **`/feature <UC-ID>`** (`.claude/commands/feature.md`) to drive it through plan → design (write/update the area's `docs/ram/design/` doc) → code → test.
3. The work is recorded back into `docs/ram/traceability.md`.

Treat the use case as the contract:
- The **use case** gives actors, trigger, main success scenario, extensions, and pre/postconditions — *what to build and the flows to test*. Its system-subject steps plus their **Associated Information** are themselves the detailed functional requirement (a use case is a high-level FR); the cross-cutting **§5.2 FRs** (`FR-LOCK-*`, `FR-SAVE-*`, …) it cites are the additional atomic "shall" statements it must honor. Together they are the *acceptance criteria* — don't restate the use-case flow as a new FR, and don't invent missing detail silently.
- **Citation is at the use-case level.** Traceability is one row per UC and tests are tagged to the UC — there is no finer (per-step) handle, so the UC's tests must cover the whole main flow **and every extension**, and a use case must stay small enough that "UC-X passes" is a meaningful statement. The extensions carry the edge-case requirements.
- The **glossary** fixes vocabulary — use the defined term in code identifiers and UI text, never a synonym.
- **The spec is authoritative but not infallible.** When a step is ambiguous, an assumption breaks against the existing code, or requirements contradict — ask a clarifying question or challenge the spec; don't silently comply or silently invent. Fix the spec and re-derive rather than diverging quietly in code. `/feature` builds this in.

Cross-cutting behavior is already specified, and some is already built — reuse it, don't reinvent per feature:
- **Locking** (`FR-LOCK-*`): section-level pessimistic locking already exists in `ram/document/`. See UC-DOC-2 / UC-DOC-6.
- **Collaboration** (`FR-COL-*`): comment threads exist in `ram/collaboration/`; real-time presence/broadcast is specified in UC-COL-1 (not yet built).
- **Validation** (`FR-VAL-*`, ReqLint): deterministic structural checks. See UC-VAL-1.

When implementing, **extend the existing RAM packages** (`ram/document`, `ram/requirement`, `ram/usecase`, `ram/glossary`, `ram/collaboration`) and the shared course/section/team/auth/email infrastructure — RAM is a module inside this codebase, not a separate system, so don't fork or duplicate the architecture "for RAM." Then map the use case back into `docs/ram/traceability.md` (frontend `apis/` + views + stores, backend `ram/*` controller/service/repository/entity, tests).

### Editing the docs

When editing anything under `docs/ram/`, the rules in `docs/ram/CLAUDE.md` apply (heading numbering, TOC regeneration, anchor slugs, FR/BR/UC ID schemes, cross-doc terminology). Run **`/build`** to verify and resync. The placeholder `docs/pulse-core/` module will carry its own nested `CLAUDE.md` once authored.

## CI

- **PR checks:** `maven-build.yml` runs `mvn package` (builds + tests backend) on PRs to `main`
- **Deploy:** `azure-webapps-deploy.yml` on push to `main` — builds frontend, bundles into Spring Boot jar, Docker image → GHCR → Azure Web App staging slot
