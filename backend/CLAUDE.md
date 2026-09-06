# Backend CLAUDE.md

## Design Philosophy

When developing new features, follow the same modeling approach used throughout this codebase:
- **Domain-Driven Design (DDD)** — model the domain first, let the domain drive the structure
- **SOLID principles** — single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **OOP best practices** — encapsulation, meaningful abstractions, favor composition over inheritance
- **Design patterns** where appropriate (e.g., Converter pattern, Specification pattern, Strategy pattern) — follow existing patterns in the codebase rather than introducing new ones without reason

## Package Layout

Each bounded context is a self-contained vertical-slice package under `team.projectpulse.<domain>` (`activity`, `evaluation`, `course`, `student`, `team`, `section`, `rubric`, `instructor`). Cross-cutting packages:

- `system/` — `Result` (the API response envelope; see Conventions below), `StatusCode` constants, `ExceptionHandlerAdvice` (global `@RestControllerAdvice`), `DataInitializer` (dev-profile seed data), `EmailService`, clock configs
- `security/` — JWT auth (RSA key pair generated at startup), `SecurityConfiguration` (URL-level rules), `authorizationmanagers/` (fine-grained ownership/membership `AuthorizationManager`s)
- `user/` — shared `PeerEvaluationUser` base class, password reset, user invitation flows

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
8. **SecurityService** (optional) — helper queried by custom `AuthorizationManager` implementations

## Domain Model Hierarchy

`Course` is the aggregate root. Cascading flows downward:

```
Course (aggregate root)
├── Criterion[]        (CascadeType.ALL)
├── Rubric[]           (CascadeType.ALL, a rubric groups criteria)
└── Section[]          (CascadeType.ALL)
    ├── Team[]         (CascadeType.ALL)
    │   └── Student[]  (no cascade — students belong to section, assigned to team)
    └── Student[]      (CascadeType.ALL)
```

- **Instructor** is independent (saved separately), associated at multiple levels: `Course` (ManyToMany + courseAdmin), `Section` (ManyToMany), `Team` (ManyToOne). Each instructor has a `defaultCourse` and `defaultSection` preference.
- **Student** and **Instructor** both extend `PeerEvaluationUser` (abstract `@Entity` with shared fields: username, name, email, password, roles).
- **Activity** and **PeerEvaluation** are independent entities (not cascaded from Course) — they reference Student and Team.
- **RAM entities** (documents, requirement artifacts, use cases, glossary, comments) are scoped to a Team.

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
- Use `ObjectNotFoundException(entityName, id)` when findById fails
- Dynamic queries built with `Specification` pattern (`*Specs` class)
- Use `UserUtils` (`system/UserUtils.java`) to get the current authenticated user's ID, role, course, or section from the JWT. Services use it to scope queries (e.g., filtering by the user's section). Don't extract user context from `SecurityContextHolder` directly — use `UserUtils` instead.

### Time Handling
- All time-dependent code must inject the `Clock` bean and use `LocalDateTime.now(clock)`, never `LocalDateTime.now()`.
- The dev profile uses a **fixed clock** (frozen at Aug 20, 2023 23:30, `America/Chicago`) so seed data weeks align. Calling `LocalDateTime.now()` directly would bypass this and break dev/test behavior.
- Prod and staging profiles use a real system clock configured to the `app.timezone` property.

### DTOs and Converters
- DTO naming: `EntityDto` (e.g., `ActivityDto`, `CourseDto`)
- Converter naming: `EntityToEntityDtoConverter` / `EntityDtoToEntityConverter`
- Converters are Spring `@Component` beans, not static utilities

### Security

Authorization has **two enforcement points**. A request that reaches data has to pass both, and each answers a different question. Most of the bugs in this area come from assuming one of them covers the other's question.

**Point 1, the route rule** (`SecurityConfiguration.securityFilterChain()`) answers *"may this caller call this URL at all?"*. Every endpoint needs one. Three ways to write it, in increasing order of what it knows:
  - a plain check with no domain knowledge: `.hasAuthority("ROLE_admin")`, `.authenticated()`, `.permitAll()`
  - `.access(someAuthorizationManager)` when the answer depends on the domain. The manager (in `security/authorizationmanagers/`) is a thin wrapper: it pulls path variables out of the request URI and delegates to a `*SecurityService` (in the domain package) that holds the actual logic, e.g. `TeamSecurityService.canAccessTeam`, `ActivitySecurityService.isActivityOwner`. **That pair is how point 1 is built, not a second defence.**
  - an endpoint with no rule at all is a **bug**: it falls through to the catch-all `.authenticated()` and becomes callable by any logged-in user, silently. Nothing currently detects this automatically, so check it by hand when adding a route (a route-coverage test is planned once the last known gap closes: see OI-47).

**Point 2, the scoped query** (in the service) answers *"is the object this request names actually in the caller's scope?"*. Load by id **and** owning team together: `findByIdAndTeamTeamId(id, teamId)`, never `findById(id)`.

**Why both, always.** A route guard reads `{teamId}` out of the URI, so it proves only that the caller belongs to *the team named in the URL*. It never proves that the object named beside it belongs to that team, so a caller passing their **own** `teamId` next to **another team's** object id sails through. The reverse fails just as badly: a scoped query with no route rule is worthless, because then the caller chooses the `teamId` you are scoping by. Neither one alone is enough. (Every cross-team defect found in September 2026 was one of the two missing: the glossary routes had neither; the document-section GET had a scoped query but no rule; the artifact and use-case lookups had a rule but no scoping.)

Working rules:
- **Put the scope in the query, not in a check after the load.** Write `findByIdAndTeamTeamId(id, teamId)` (Spring Data derives it from the method name; `Team`'s primary key field is `teamId`, hence the doubled word), not `findById(id)` followed by a `getTeam()` comparison. The check-after version refuses the request just as correctly, but it leaves the unscoped load in the codebase as the thing the next person copies, and it can be deleted without any test failing. Precedents to copy: `RequirementArtifactRepository`, `CommentRepository.findByIdAndCommentThreadIdAndCommentThreadTeamTeamId`, and `DocumentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId`, which also binds the child to the parent named in the URL.
- **Ids arriving in the request body pass neither point.** A `sourceArtifactId`, a `primaryActorId`, or a DTO `id` in a payload is covered by no route rule and by no lookup scoping. Re-resolve every one of them through a team-scoped finder in the service (see `UseCaseService.requireActorsInTeam`), and never scope by a `teamId` that itself came from the body, because the caller sets it (OI-46).
- **The smell to grep for:** a service method that accepts `Integer teamId` and never mentions it in the body. That was the exact shape of the September 2026 cross-team bypass, in every RAM service at once.
- **Test both refusals.** A non-member stopped at the route is `403`; a member passing another team's object id through their own team's URL is `404`. See the `_NotSameTeam` and `_OtherTeams...ThroughOwnTeamUrl` tests in the RAM controller tests.
- Ownership = user created the resource; Membership = user belongs to the same course/section/team
- Full rationale: [Authorization in the architecture-of-record](../docs/design/architectural-design.md#authorization)

### Database
- Dev profile: `ddl-auto: create` + `DataInitializer` seeds data on every restart
- Prod/staging: Flyway migrations only (`src/main/resources/db/migration/V*.sql`)
- When adding schema changes for production, create a new `V<n>__description.sql` migration file
- When adding a new domain, add representative seed data to `DataInitializer` for dev and integration testing

### Spring Profiles
`dev` (default — local MySQL + Mailpit, fixed clock, `ddl-auto: create` + `DataInitializer`), `staging`, and `prod` (Azure Key Vault for secrets, Flyway migrations). Per-profile clock and schema behavior is detailed in **Time Handling** and **Database** above.

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
