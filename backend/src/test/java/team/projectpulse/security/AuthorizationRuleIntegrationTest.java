package team.projectpulse.security;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers what the route rules do when they cannot answer their question: an id that names no row, an id that
 * is not a number at all, and a caller whose own record is not linked to anything yet.
 *
 * <p>Each of those used to be a 500. The {@code *SecurityService} threw {@code ObjectNotFoundException} from
 * inside an {@code AuthorizationManager}, which runs ahead of the {@code DispatcherServlet} and so never
 * reaches {@code ExceptionHandlerAdvice}; the exception escaped the filter chain into the container's error
 * dispatch. Besides being the wrong response shape, it told an attacker which ids exist: 500 meant "no such
 * team", 403 meant "that team exists and is not yours". Both now answer 403.
 */
@DisplayName("Integration tests for the route authorization rules")
class AuthorizationRuleIntegrationTest extends AbstractIntegrationTest {

    private static final int NO_SUCH_ID = 999999;

    @Autowired
    MockMvc mockMvc;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String adminBingyangToken;

    String studentJohnToken;


    @BeforeEach
    void setUp() throws Exception {
        this.adminBingyangToken = login("b.wei@abc.edu");
        this.studentJohnToken = login("j.smith@abc.edu");
    }

    private String login(String email) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(email, "123456"))).andReturn();
        JSONObject json = new JSONObject(mvcResult.getResponse().getContentAsString());
        return "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("A team id that names no row is denied, not a server error")
    void adminIsForbiddenForATeamThatDoesNotExist() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/" + NO_SUCH_ID).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DisplayName("A team id that is not a number is denied, not a server error")
    void adminIsForbiddenForATeamIdThatIsNotANumber() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/not-a-number").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @DisplayName("A missing id is denied on every shape of rule, not only the team ones")
    void adminIsForbiddenForOtherObjectsThatDoNotExist() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/activities/" + NO_SUCH_ID).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));

        this.mockMvc.perform(get(this.baseUrl + "/sections/" + NO_SUCH_ID).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));

        this.mockMvc.perform(get(this.baseUrl + "/rubrics/" + NO_SUCH_ID).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));

        this.mockMvc.perform(get(this.baseUrl + "/students/" + NO_SUCH_ID).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @DisplayName("A nested RAM route with a missing team id is denied, not a server error")
    void studentIsForbiddenForDocumentsOfATeamThatDoesNotExist() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/" + NO_SUCH_ID + "/documents/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @DisplayName("The evaluator route still resolves, though it spells the student id {evaluatorId}")
    void studentReadsTheirOwnEvaluationsThroughTheEvaluatorRoute() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/evaluators/4/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS));
    }

    @Test
    @DisplayName("Failing closed did not close the door on the members themselves")
    void studentStillReadsTheirOwnTeamButNotAnother() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.teamId").value(1));

        this.mockMvc.perform(get(this.baseUrl + "/teams/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

}
