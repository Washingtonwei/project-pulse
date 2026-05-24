# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Project Pulse is a web application for managing senior design / capstone course projects. Students submit weekly activity reports and peer evaluations; instructors monitor progress via dashboards. It also includes a Requirements Authoring Management (RAM) module that lets student teams define software requirements before coding — collaborative requirement documents, use cases, glossaries, and traceability.

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

**RAM domain** (`ram/`): Requirements Authoring Management — originally a separate project, merged into Project Pulse to reuse the existing course/section/team/student infrastructure. Sub-packages: `document/` (requirement documents with section-level pessimistic locking), `requirement/` (artifacts, traceability links), `usecase/`, `glossary/`, `collaboration/` (comment threads)

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

## CI

- **PR checks:** `maven-build.yml` runs `mvn package` (builds + tests backend) on PRs to `main`
- **Deploy:** `azure-webapps-deploy.yml` on push to `main` — builds frontend, bundles into Spring Boot jar, Docker image → GHCR → Azure Web App staging slot
