package team.projectpulse.system;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.AbstractIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The actuator endpoints sit outside ${api.endpoint.base-url}, so they are not covered by the API
 * rules or by the deny-by-default catch-all: they need rules of their own, and until 2026-09-06 they
 * had none, which left env/configprops/heapdump readable by anonymous callers in production (TD-1).
 * <p>
 * These tests run under the dev profile, which deliberately exposes the full actuator surface, so a
 * refusal here is the security rule doing its job rather than the endpoint being unexposed. In the
 * deployed profiles the exposure list is the safe baseline in application.yml as well, and these
 * rules are the second layer behind it.
 */
@DisplayName("Integration tests for actuator endpoint authorization")
class ActuatorSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String adminToken;

    String studentToken;

    @BeforeEach
    void setUp() throws Exception {
        this.adminToken = login("b.wei@abc.edu");
        this.studentToken = login("j.smith@abc.edu");
    }

    private String login(String username) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(username, "123456"))).andReturn();
        JSONObject json = new JSONObject(result.getResponse().getContentAsString());
        return "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void testHealthIsAnonymous() throws Exception {
        // The platform probe cannot authenticate, so health must stay open.
        this.mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void testInfoIsAnonymous() throws Exception {
        this.mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void testEnvIsRefusedAnonymously() throws Exception {
        // This is the endpoint that leaked resolved Key Vault secrets in production.
        this.mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testConfigpropsIsRefusedAnonymously() throws Exception {
        this.mockMvc.perform(get("/actuator/configprops"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoggersIsRefusedAnonymously() throws Exception {
        // Writable at runtime, so an anonymous caller could otherwise turn on TRACE logging.
        this.mockMvc.perform(get("/actuator/loggers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEndpointListingIsRefusedAnonymously() throws Exception {
        // The discovery listing is itself an endpoint, and it names every other one.
        this.mockMvc.perform(get("/actuator"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEnvIsForbiddenForNonAdmin() throws Exception {
        this.mockMvc.perform(get("/actuator/env").header(HttpHeaders.AUTHORIZATION, this.studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEnvIsAllowedForAdmin() throws Exception {
        this.mockMvc.perform(get("/actuator/env").header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(status().isOk());
    }

}
