package team.projectpulse.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
class WebConfigSpaForwardingTest {

    @Autowired
    MockMvc mockMvc;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

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
