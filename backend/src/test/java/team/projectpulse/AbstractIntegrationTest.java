package team.projectpulse;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import team.projectpulse.ram.document.DocumentSectionRepository;

/**
 * Base class for all integration tests. Provides:
 * <ul>
 *   <li>Shared MySQL and Mailpit containers via {@link SharedContainers} (started once per JVM)</li>
 *   <li>{@code @Transactional} rollback after each test — restores DataInitializer-seeded state
 *       without the cost of {@code @DirtiesContext} (which would recreate the entire Spring context)</li>
 *   <li>Common annotations: {@code @SpringBootTest}, {@code @AutoConfigureMockMvc},
 *       {@code @ActiveProfiles("dev")}, {@code @Tag("integration")}</li>
 *   <li>{@link #srsSectionId(String)} and {@link #frListId()}, which find team 1's seeded SRS sections by key</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Tag("integration")
@Transactional
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql = SharedContainers.MYSQL;


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", SharedContainers.MAILPIT::getHost);
        registry.add("spring.mail.port", () -> SharedContainers.MAILPIT.getMappedPort(1025));
    }

    // Private and named apart from the documentSectionRepository field several subclasses declare, so it hides none
    @Autowired
    private DocumentSectionRepository srsSectionLookup;

    /**
     * The id of team 1's SRS (document 1) section with {@code sectionKey}. Tests find SRS sections by key, so a template
     * change that adds or moves a section renumbers no test (docs/design/tpl.md). For example,
     * {@code srsSectionId("NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")} is the list that holds FR-user-authentication.
     */
    protected int srsSectionId(String sectionKey) {
        return Math.toIntExact(this.srsSectionLookup.findByDocumentIdAndSectionKey(1L, sectionKey).orElseThrow().getId());
    }

    /** Team 1's functional requirement list, NON_USE_CASE_FUNCTIONAL_REQUIREMENTS, which the seed fills with three FRs. */
    protected int frListId() {
        return srsSectionId("NON_USE_CASE_FUNCTIONAL_REQUIREMENTS");
    }

}
