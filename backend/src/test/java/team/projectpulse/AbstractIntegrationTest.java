package team.projectpulse;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base class for all integration tests. Provides:
 * <ul>
 *   <li>Shared MySQL and Mailpit containers via {@link SharedContainers} (started once per JVM)</li>
 *   <li>{@code @Transactional} rollback after each test — restores DataInitializer-seeded state
 *       without the cost of {@code @DirtiesContext} (which would recreate the entire Spring context)</li>
 *   <li>Common annotations: {@code @SpringBootTest}, {@code @AutoConfigureMockMvc},
 *       {@code @ActiveProfiles("dev")}, {@code @Tag("integration")}</li>
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

}
