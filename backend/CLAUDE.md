# Backend CLAUDE.md

## Design Philosophy

When developing new features, follow the same modeling approach used throughout this codebase:
- **Domain-Driven Design (DDD)** — model the domain first, let the domain drive the structure
- **SOLID principles** — single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **OOP best practices** — encapsulation, meaningful abstractions, favor composition over inheritance
- **Design patterns** where appropriate (e.g., Converter pattern, Specification pattern, Strategy pattern) — follow existing patterns in the codebase rather than introducing new ones without reason

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
- URL-level rules go in `SecurityConfiguration.securityFilterChain()`
- Fine-grained authorization uses a **two-layer pattern**:
  1. `*SecurityService` (in the domain package) — contains the actual authorization logic (e.g., `ActivitySecurityService.isActivityOwner()`)
  2. `*AuthorizationManager` (in `security/authorizationmanagers/`) — thin wrapper that extracts path variables from the request URI and delegates to the corresponding `*SecurityService`
- When adding a secured endpoint, create both: the `SecurityService` method with the business logic, and the `AuthorizationManager` that wires it to the URL pattern in `SecurityConfiguration`
- Ownership = user created the resource; Membership = user belongs to the same course/section/team

### Database
- Dev profile: `ddl-auto: create` + `DataInitializer` seeds data on every restart
- Prod/staging: Flyway migrations only (`src/main/resources/db/migration/V*.sql`)
- When adding schema changes for production, create a new `V<n>__description.sql` migration file
- When adding a new domain, add representative seed data to `DataInitializer` for dev and integration testing

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
```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for Foo API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
public class FooIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JsonMapper jsonMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Container @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

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

Integration tests use Testcontainers (MySQL 8.0), seed data from `DataInitializer`, and authenticate via HTTP Basic to get JWT tokens for subsequent requests. Tests verify both the `flag` and `code` fields in the `Result` response. Use `@DirtiesContext` on tests that modify state and could affect other tests.
