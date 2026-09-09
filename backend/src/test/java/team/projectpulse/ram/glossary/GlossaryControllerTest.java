package team.projectpulse.ram.glossary;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class GlossaryControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    String studentJohnToken; // John is on team 1 in section 2

    String studentWoodyToken; // Woody is on team 2 in section 2

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.smith@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("w.allen@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentWoodyToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void findGlossaryTermById_NotSameTeam() throws Exception {
        long team1GlossaryTermId = fetchTeam1GlossaryTermId();

        this.mockMvc.perform(get(this.baseUrl + "/teams/1/glossary-terms/" + team1GlossaryTermId).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void addGlossaryTerm_NotSameTeam() throws Exception {
        String json = """
                {
                    "type": "GLOSSARY_TERM",
                    "title": "Sprint",
                    "content": "A fixed-length iteration in which the team delivers a working increment.",
                    "notes": ""
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/glossary-terms").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void updateGlossaryTermDefinition_NotSameTeam() throws Exception {
        long team1GlossaryTermId = fetchTeam1GlossaryTermId();

        String json = """
                {
                    "type": "GLOSSARY_TERM",
                    "title": "Artifact",
                    "content": "A definition written by someone outside the owning team."
                }
                """;
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/glossary-terms/" + team1GlossaryTermId).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void renameGlossaryTerm_NotSameTeam() throws Exception {
        long team1GlossaryTermId = fetchTeam1GlossaryTermId();

        String json = """
                {
                    "type": "GLOSSARY_TERM",
                    "title": "Renamed by an outsider",
                    "content": "A definition."
                }
                """;
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/glossary-terms/" + team1GlossaryTermId + "/rename").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void findGlossaryTermById_OtherTeamsTermThroughOwnTeamUrl() throws Exception {
        // John is a student on team 1; the glossary term belongs to team 2. The URL names team 1,
        // so the membership guard passes and only service-layer scoping can stop this. This
        // isolates the service layer from the route rules. The PATCH bodies below carry a full
        // RequirementArtifactDto because the write routes are @Valid: an incomplete body is
        // rejected as a 400 before the service runs, which would hide the 404 under test.
        long team2GlossaryTermId = fetchTeam2GlossaryTermId();

        this.mockMvc.perform(get(this.baseUrl + "/teams/1/glossary-terms/" + team2GlossaryTermId).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }

    @Test
    void updateGlossaryTermDefinition_OtherTeamsTermThroughOwnTeamUrl() throws Exception {
        long team2GlossaryTermId = fetchTeam2GlossaryTermId();

        String json = """
                {
                    "type": "GLOSSARY_TERM",
                    "title": "Deliverable",
                    "content": "A definition written into another team's glossary."
                }
                """;
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/glossary-terms/" + team2GlossaryTermId).contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }

    @Test
    void renameGlossaryTerm_OtherTeamsTermThroughOwnTeamUrl() throws Exception {
        long team2GlossaryTermId = fetchTeam2GlossaryTermId();

        String json = """
                {
                    "type": "GLOSSARY_TERM",
                    "title": "Renamed through another team's URL",
                    "content": "A definition."
                }
                """;
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/glossary-terms/" + team2GlossaryTermId + "/rename").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }

    /**
     * Looks up team 1's glossary term id through team 1's own search endpoint, as a team 1 student,
     * rather than hard-coding a seeded id that future seed changes would silently invalidate.
     */
    private long fetchTeam1GlossaryTermId() throws Exception {
        return fetchGlossaryTermId(1, this.studentJohnToken);
    }

    /**
     * The same lookup for team 2, as a team 2 student, so the cross-team cases name a term that
     * really belongs to the other team.
     */
    private long fetchTeam2GlossaryTermId() throws Exception {
        return fetchGlossaryTermId(2, this.studentWoodyToken);
    }

    private long fetchGlossaryTermId(int teamId, String token) throws Exception {
        String searchJson = """
                {
                    "type": "GLOSSARY_TERM"
                }
                """;
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/" + teamId + "/requirement-artifacts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andReturn();
        JSONObject json = new JSONObject(result.getResponse().getContentAsString());
        return json.getJSONObject("data").getJSONArray("content").getJSONObject(0).getLong("id");
    }

}
