# Backend CLAUDE.md

## Design Philosophy

When developing new features, follow the same modeling approach used throughout this codebase:
- **Domain-Driven Design (DDD)** — model the domain first, let the domain drive the structure
- **SOLID principles** — single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **OOP best practices** — encapsulation, meaningful abstractions, favor composition over inheritance
- **Design patterns** where appropriate (e.g., Converter pattern, Specification pattern, Strategy pattern) — follow existing patterns in the codebase rather than introducing new ones without reason

## Package Layout

Each bounded context is a self-contained vertical-slice package under `team.projectpulse.<domain>` (`activity`, `evaluation`, `course`, `student`, `team`, `section`, `rubric`, `instructor`). Cross-cutting packages:

- `system/` — `Result` (the API response envelope; see Conventions below), `StatusCode` constants, `ExceptionHandlerAdvice` (global `@RestControllerAdvice`), `EmailService`, clock configs
- `security/` — JWT auth (RSA key pair generated at startup), `SecurityConfiguration` (URL-level rules), `authorizationmanagers/` (fine-grained ownership/membership `AuthorizationManager`s)
- `user/` — shared `PeerEvaluationUser` base class, password reset, user invitation flows
- `seed/`: `DataInitializer` (dev-profile seed data), kept outside `system` because it depends on every feature; the one package exempt from the feature-locality rule

**RAM module** (`ram/`) — Requirements Authoring & Management, merged in to reuse the course/section/team/student infrastructure. Sub-packages: `document/` (requirement documents with section-level pessimistic locking), `requirement/` (artifacts, traceability links), `usecase/`, `glossary/`, `collaboration/` (comment threads). Extend these packages — don't fork the architecture for RAM.

The binding conventions every package follows are normative in the [architecture-of-record's Architectural Conventions](../docs/design/architectural-design.md#architectural-conventions); the sections below are the working detail.

## Adding a New Feature/Entity

**Reference implementation:** Use the `activity` package as the canonical example. It demonstrates the full vertical slice: Entity, Repository, Service, Controller, DTO, Converters, Specs, and SecurityService. Read it end-to-end before building a new domain.

The backend follows the **DDD** approach described above. Each domain (bounded context) lives in its own package under `team.projectpulse.<domain>` and owns its full vertical slice. A complete domain package includes:

1. **Entity** — JPA entity with `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`. No Lombok; write explicit constructors, getters, setters.
2. **Repository** — extends `JpaRepository` (and `JpaSpecificationExecutor` if dynamic search is needed)
3. **Service** — `@Service @Transactional`. Throw `ObjectNotFoundException` for missing entities.
4. **Controller** — `@RestController @RequestMapping("${api.endpoint.base-url}/<feature>")`. Every method returns a `Result` object.
5. **DTO** — plain Java class (or record) representing the API shape
6. **Converters** — one `EntityToEntityDtoConverter` and one `EntityDtoToEntityConverter`, both implementing Spring's `Converter<S,T>` interface and annotated `@Component`
7. **Specs** (optional) — static methods returning `Specification<Entity>` for dynamic search criteria
8. **SecurityService** (optional): helper queried by custom `AuthorizationManager` implementations. Returns booleans and never throws; see **Security** below

## Domain Model Hierarchy

Quick reference; the normative version, with the associations and the user inheritance, is the architecture-of-record's [Domain & aggregate model](../docs/design/architectural-design.md#domain--aggregate-model). Check it before adding an entity or a cascade.

```
Course (aggregate root)
├── Criterion[]        (CascadeType.ALL)
├── Rubric[]           (CascadeType.ALL; a rubric groups criteria)
└── Section[]          (CascadeType.ALL; each references one of its course's rubrics)
    ├── Team[]         (CascadeType.ALL)
    │   └── Student[]  (no cascade; students belong to the course section and are assigned to a team)
    └── Student[]      (CascadeType.ALL)
```

- `Instructor`, `Activity`, and `PeerEvaluation` sit outside the cascade and are saved separately.
- `Student` and `Instructor` extend the abstract `PeerEvaluationUser` (username, name, email, password, roles).
- `Student.team` is optional (teams are assigned after registration), so null-check it; `Student.section` is not.
- RAM entities are scoped to a `Team`.

## Conventions

### API Response Envelope
Every controller method returns a `Result` object (`system/Result.java`) with four fields:
- `flag` (boolean) — `true` for success, `false` for failure
- `code` (Integer) — status code from `StatusCode.java`
- `message` (String) — human-readable response message
- `data` (Object) — the response payload (DTO, Page, Map, or null)

Available status codes are defined as constants in `system/StatusCode.java`.

Success example: `new Result(true, StatusCode.SUCCESS, "Find activity successfully", activityDto)`
Error handling is centralized in `ExceptionHandlerAdvice` — services throw exceptions, the advice maps them to `Result` objects with the appropriate `StatusCode`.

### Controllers
- Use `@Valid` on `@RequestBody` DTO parameters for input validation
- Search endpoints use `POST /search` with a `Map<String, String>` body + Spring `Pageable`

### Services
- Constructor injection (no `@Autowired` on fields)
- Throw `ObjectNotFoundException(entityName, id)` when a lookup finds nothing, except in a `*SecurityService`, which returns `false` (see **Security** below)
- Dynamic queries built with `Specification` pattern (`*Specs` class)
- Get the current user's id, roles, course, or course section from `UserUtils` (`system/UserUtils.java`), never from `SecurityContextHolder`. Services use it to scope queries (e.g., filtering by the user's course section).

### Time Handling
- **Calendar time** (weeks, deadlines, reminders, audit stamps): inject the `Clock` bean and use `LocalDateTime.now(clock)`, never `LocalDateTime.now()`.
- **Elapsed time** (JWT expiry, edit-lock leases): use `Instant.now()`. The dev clock is fixed at 2023-08-20 23:30, so a lease timed by it would never lapse.
- Why the two differ, and the per-profile clocks: [Architectural conventions › Time](../docs/design/architectural-design.md#architectural-conventions).

### DTOs and Converters
- DTO naming: `EntityDto` (e.g., `ActivityDto`, `CourseDto`)
- Converter naming: `EntityToEntityDtoConverter` / `EntityDtoToEntityConverter`
- Converters are Spring `@Component` beans, not static utilities

### Security

Authorization has **two enforcement points**, and every request that reaches data must pass both: the route rule (*may this caller call this URL at all?*) and the scoped query (*is the object this request names in the caller's scope?*). The rules, why neither point is enough alone, and the September 2026 defects that proved it are normative in the architecture-of-record's [Authorization](../docs/design/architectural-design.md#authorization). Read it before adding an endpoint or touching `security/`. What follows is how to apply it in this codebase.

**Point 1, the route rule** (`SecurityConfiguration.securityFilterChain()`). Every endpoint needs one.
- Use a plain check (`.hasAuthority("ROLE_admin")`, `.authenticated()`, `.permitAll()`) when the answer needs no domain knowledge, and `.access(someAuthorizationManager)` when it does. The manager (in `security/authorizationmanagers/`) is a thin wrapper that delegates to a `*SecurityService` in the domain package, e.g. `TeamSecurityService.canAccessTeam`, `ActivitySecurityService.isActivityOwner`.
- Read ids with `PathVariables.readId(context, "teamId")`, which reads `context.getVariables()`. Never parse `getRequestURI()`.
- A `*SecurityService` returns `false` for anything it cannot resolve and never throws. Null-check only associations that are genuinely optional; declare mandatory ones `@ManyToOne(optional = false)`.
- A `403` on a brand-new endpoint usually means you forgot its rule: the catch-all under the API base URL is `.denyAll()`.
- Adding an actuator endpoint means checking both the per-profile exposure list and the `/actuator/**` rules (see [Observability & operations](../docs/design/architectural-design.md#observability--operations)).

**Point 2, the scoped query** (in the service).
- Load by id and owning team in one query: `findByIdAndTeamTeamId(id, teamId)`, never `findById(id)` followed by a `getTeam()` comparison. Spring Data derives it from the method name; `Team`'s primary key field is `teamId`, hence the doubled word. Precedents to copy: `RequirementArtifactRepository`, `CommentRepository.findByIdAndCommentThreadIdAndCommentThreadTeamTeamId`, and `DocumentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId`, which also binds the child to the parent named in the URL.
- When the URL carries no container id (a flat route such as `/activities/{activityId}`, or `POST /activities/search`), derive the scope from the caller through `UserUtils`, and let a `teamId` in the search criteria filter inside that scope, never set it. See `ActivityService.findByCriteria`.
- Re-resolve every id in a request body (a `sourceArtifactId`, a `primaryActorId`, a DTO `id`) through a team-scoped finder, as `UseCaseService.requireActorsInTeam` does. Never scope by a `teamId` that came from the body (OI-46).

**The smell to grep for:** a service method that accepts `Integer teamId` and never mentions it in the body. That was the exact shape of the September 2026 cross-team bypass, in every RAM service at once.

**Test both refusals.** A non-member stopped at the route is `403`; a member passing another team's object id through their own team's URL is `404`. An id that names nothing at all is `403` as well, because the route rule refuses before the service runs (`AuthorizationRuleIntegrationTest`). See the `_NotSameTeam` and `_OtherTeams...ThroughOwnTeamUrl` tests in the RAM controller tests.

### Database
- `dev` rebuilds the schema (`ddl-auto: create`) and reseeds from `DataInitializer` on every restart; `staging`/`prod` apply Flyway migrations only (`src/main/resources/db/migration/V*.sql`)
- When adding schema changes for production, create a new `V<n>__description.sql` migration file
- When adding a new domain, add representative seed data to `DataInitializer` for dev and integration testing

### Spring Profiles
`dev` (the default; local MySQL and Mailpit), `staging`, and `prod` (secrets from Azure Key Vault). What each profile changes about the schema, seed data, and clock is in **Database** and **Time Handling** above, and normatively in [Architectural conventions](../docs/design/architectural-design.md#architectural-conventions).

## Testing Patterns

### Unit Tests (`*ServiceTest.java`)
```java
@ExtendWith(MockitoExtension.class)
class FooServiceTest {
    @Mock FooRepository fooRepository;
    @Mock UserUtils userUtils;
    @InjectMocks FooService fooService;

    @BeforeEach
    void setUp() {
        // Build domain objects (instructors, sections, teams, students) in memory
    }

    @Test
    void testFindFooByIdSuccess() {
        // Use BDDMockito: given(...).willReturn(...)
        // Assertions with AssertJ: assertThat(...)
    }
}
```

### Integration Tests (`*IntegrationTest.java`)

All integration tests extend `AbstractIntegrationTest`, which provides:
- **Shared containers** — `SharedContainers` starts a single MySQL and Mailpit container per JVM (singleton pattern), shared by all test classes
- **`@Transactional` rollback** — each test runs in a transaction that rolls back automatically, restoring the DataInitializer-seeded state without needing `@DirtiesContext`
- **Common annotations** — `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("dev")`, `@Tag("integration")`

```java
@DisplayName("Integration tests for Foo API endpoints")
public class FooIntegrationTest extends AbstractIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JsonMapper jsonMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String adminToken;
    String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login via HTTP Basic to get JWT tokens for different roles
        // e.g., POST baseUrl + "/users/login" with httpBasic("b.wei@abc.edu", "123456")
        // Extract token from JSON response: json.getJSONObject("data").getString("token")
    }

    @Test
    void testFindFooByCriteria() throws Exception {
        this.mockMvc.perform(post(this.baseUrl + "/foos/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS));
    }
}
```

Integration tests use Testcontainers (MySQL 8.0), seed data from `DataInitializer`, and authenticate via HTTP Basic to get JWT tokens for subsequent requests. Tests verify both the `flag` and `code` fields in the `Result` response. Do **not** use `@DirtiesContext` — the `@Transactional` rollback on the base class handles database reset automatically.
