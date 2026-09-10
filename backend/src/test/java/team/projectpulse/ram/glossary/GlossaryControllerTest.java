package team.projectpulse.ram.glossary;

import org.hamcrest.Matchers;
import org.json.JSONArray;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * The four glossary-term routes are guarded at two independent points, and a defect in either is invisible to a
 * test of the other, so each is covered on its own.
 *
 * <p><strong>The route rule</strong> ({@code SecurityConfiguration}, bound to
 * {@code teamMembershipAuthorizationManager}) reads {@code teamId} out of the URL and proves only that the caller
 * belongs to that team. A caller who is not a member is stopped here with {@code 403}, one case per route. Until
 * 2026-09-09 these four routes matched no rule at all and fell to the API catch-all, which is OI-47.
 *
 * <p><strong>The team-and-type scoped finder</strong> in {@code GlossaryService} proves that the term belongs to
 * that team. A member of one team who passes another team's term id through their own team's URL satisfies the
 * route rule and is stopped here instead, with {@code 404}, on the three routes carrying a {@code glossaryTermId}
 * (the POST has none). Those bodies carry a full {@code RequirementArtifactDto} because the write routes are
 * {@code @Valid}: an incomplete body is rejected as a {@code 400} before the service runs, which would hide the
 * {@code 404} under test.
 *
 * <p><strong>The create route carries no term id</strong>, so it has no cross-team case and the refusal case is
 * the only thing that covers it. That leaves it the one route where a rule bound to a manager stricter than
 * membership, which would refuse the owning team as well, fails no test: the refusal case passes either way,
 * since a stricter rule refuses an outsider just as well. {@code addGlossaryTerm_SameTeam} closes that, and also
 * holds the line on the type and key being the server's rather than the body's.
 *
 * <p>Term ids are looked up through the search endpoint rather than hard-coded, so seed changes cannot silently
 * invalidate the cases.
 */
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
    void addGlossaryTerm_SameTeam() throws Exception {
        String json = """
                {
                    "type": "GLOSSARY_TERM",
                    "title": "Increment",
                    "content": "The working software a team delivers at the end of a sprint.",
                    "notes": ""
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/glossary-terms").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add glossary term successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Increment"))
                .andExpect(jsonPath("$.data.type").value("GLOSSARY_TERM"))
                .andExpect(jsonPath("$.data.artifactKey").value(Matchers.startsWith("GLO-"))); // Minted from the team GLO sequence, never taken from the body. See GlossaryTermCreationIntegrationTest.
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
     *
     * <p>The refusal cases spend this round trip on an id the route rule never reaches, which is
     * deliberate: it decides how a regression reads. Weaken the rule to {@code .authenticated()} and
     * an outsider holding a real id is answered {@code 200}, so the test fails saying an outsider
     * read another team's term. A made-up id would be answered {@code 404} by the scoped finder
     * instead, which also fails the assertion but reads as a missing row rather than as a breach.
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
                .andExpect(jsonPath("$.flag").value(true))
                .andReturn();
        JSONObject json = new JSONObject(result.getResponse().getContentAsString());
        JSONArray terms = json.getJSONObject("data").getJSONArray("content");
        // Checked here so that a seed carrying no glossary term for this team fails as itself, rather
        // than as a JSONException from the line below in a test that is about authorization.
        assertThat(terms.length())
                .withFailMessage("Team %d has no seeded glossary term, so there is no id for this case to use.", teamId)
                .isGreaterThan(0);
        return terms.getJSONObject(0).getLong("id");
    }

}
