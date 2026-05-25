package team.projectpulse.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import team.projectpulse.SharedContainers;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
class WebConfigSpaForwardingTest {

    @Autowired
    MockMvc mockMvc;

    @ServiceConnection
    static final MySQLContainer<?> mysql = SharedContainers.MYSQL;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", SharedContainers.MAILPIT::getHost);
        registry.add("spring.mail.port", () -> SharedContainers.MAILPIT.getMappedPort(1025));
    }

    @Test
    void shouldForwardThreeSegmentSpaRouteToIndexHtml() throws Exception {
        this.mockMvc.perform(get("/ram/documents/1"))
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void shouldNotForwardApiRoute() throws Exception {
        this.mockMvc.perform(get("/api/v1/teams/1/documents/1"))
                .andExpect(result -> assertNull(result.getResponse().getForwardedUrl()));
    }

    @Test
    void shouldNotForwardSwaggerUiRoute() throws Exception {
        this.mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> assertNull(result.getResponse().getForwardedUrl()));
    }

}
