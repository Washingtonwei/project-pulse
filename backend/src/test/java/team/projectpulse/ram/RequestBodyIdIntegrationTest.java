package team.projectpulse.ram;

import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.ResultActions;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * An id arriving in a request body must never reach {@code save()}.
 *
 * <p>Every RAM create route takes its {@code teamId} from the path, and the service stamps that team onto the
 * entity before saving it. That is correct for a genuine create. It becomes a cross-team overwrite the moment the
 * entity carries an id, because a non-null id turns {@code save()} into a merge: the row named by the body is
 * rewritten with the caller's content <em>and reassigned to the caller's team</em>. Neither the route rules nor
 * the team-scoped finders see it, because the id never appears in the URL.
 *
 * <p>These probes each name a real artifact belonging to team 2 and submit it through a team 1 create route as
 * student John, who is on team 1. They assert the security property rather than a status code, so they hold
 * whether the request is refused or the borrowed id is ignored.
 */
public class RequestBodyIdIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    EntityManager entityManager;

    String studentJohnToken; // John is on team 1 in section 2

    @Value("${api.endpoint.base-url}")
    String baseUrl;


    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.smith@abc.edu", "123456")));
        MvcResult mvcResult = resultActions.andReturn();
        JSONObject json = new JSONObject(mvcResult.getResponse().getContentAsString());
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    /**
     * Finds a seeded artifact owned by team 2 by title, and guards that it really is team 2's, so these probes
     * fail loudly rather than silently testing nothing if the seed data changes.
     */
    private RequirementArtifact team2Artifact(String title) {
        RequirementArtifact artifact = this.requirementArtifactRepository.findAll().stream()
                .filter(candidate -> title.equals(candidate.getTitle()))
                .filter(candidate -> candidate.getTeam() != null && candidate.getTeam().getTeamId() == 2)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No team 2 artifact titled '" + title + "' in the seed data"));
        assertThat(artifact.getId()).isNotNull();
        return artifact;
    }

    /**
     * Re-reads an artifact straight from the database, past the persistence context this test shares with the
     * request, and asserts that team 2 still owns it with its seeded content intact.
     */
    private void assertStillOwnedByTeam2(Long artifactId, String expectedTitle) {
        this.entityManager.flush();
        this.entityManager.clear();
        RequirementArtifact artifact = this.requirementArtifactRepository.findById(artifactId).orElseThrow();
        assertThat(artifact.getTeam().getTeamId()).isEqualTo(2);
        assertThat(artifact.getTitle()).isEqualTo(expectedTitle);
    }

    @Test
    @DisplayName("A requirement-artifact create carrying another team's artifact id must not overwrite it")
    void testStudentJohnCannotOverwriteAnotherTeamsArtifactThroughCreate() throws Exception {
        RequirementArtifact victim = team2Artifact("Data export");

        String json = """
                {
                    "id": %d,
                    "type": "FUNCTIONAL_REQUIREMENT",
                    "title": "Overwritten",
                    "content": "Overwritten content"
                }
                """.formatted(victim.getId());

        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString());

        // A rejected request wrote nothing, and reading the database back through a transaction the failure has
        // marked rollback-only would throw, so the read only runs when the request succeeded.
        if (response.getBoolean("flag")) {
            assertThat(response.getJSONObject("data").getLong("id")).isNotEqualTo(victim.getId());
            assertStillOwnedByTeam2(victim.getId(), "Data export");
        }
    }

    @Test
    @DisplayName("A glossary-term create carrying another team's term id must not overwrite it")
    void testStudentJohnCannotOverwriteAnotherTeamsGlossaryTermThroughCreate() throws Exception {
        RequirementArtifact victim = team2Artifact("Deliverable");

        String json = """
                {
                    "id": %d,
                    "type": "GLOSSARY_TERM",
                    "title": "Overwritten",
                    "content": "Overwritten definition"
                }
                """.formatted(victim.getId());

        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/teams/1/glossary-terms").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString());

        if (response.getBoolean("flag")) {
            assertThat(response.getJSONObject("data").getLong("id")).isNotEqualTo(victim.getId());
            assertStillOwnedByTeam2(victim.getId(), "Deliverable");
        }
    }

    @Test
    @DisplayName("A use-case create carrying another team's artifact id must not overwrite it")
    void testStudentJohnCannotOverwriteAnotherTeamsArtifactThroughUseCaseCreate() throws Exception {
        // UseCase.artifact is @OneToOne(cascade = ALL) @MapsId, so the use case and its artifact share an id, and
        // UseCaseDtoToUseCaseConverter reads that id straight from the body for both.
        RequirementArtifact victim = team2Artifact("Client");

        String json = """
                {
                    "id": %d,
                    "teamId": 1,
                    "title": "Overwritten",
                    "description": "Overwritten description",
                    "trigger": "The Student does something",
                    "primaryActorId": %d,
                    "secondaryActorIds": [],
                    "preconditions": [],
                    "postconditions": [],
                    "mainSteps": []
                }
                """.formatted(victim.getId(), team1PrimaryActorId());

        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString());

        if (response.getBoolean("flag")) {
            assertThat(response.getJSONObject("data").getLong("id")).isNotEqualTo(victim.getId());
            assertStillOwnedByTeam2(victim.getId(), "Client");
        }
    }

    /**
     * A stakeholder owned by team 1, so the use-case probe fails on the id it is actually testing rather than on
     * an unrelated cross-team actor check.
     */
    private Long team1PrimaryActorId() {
        return this.requirementArtifactRepository.findAll().stream()
                .filter(candidate -> "Student".equals(candidate.getTitle()))
                .filter(candidate -> candidate.getTeam() != null && candidate.getTeam().getTeamId() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No team 1 stakeholder titled 'Student' in the seed data"))
                .getId();
    }

    @Test
    @DisplayName("A LIST section update naming another team's artifact must not pull that artifact into the section")
    void testStudentJohnCannotAdoptAnotherTeamsArtifactThroughSectionUpdate() throws Exception {
        // DocumentSection.requirementArtifacts is @OneToMany(cascade = ALL) and replaceAllRequirementArtifacts
        // points every artifact's back-reference at the section, so an artifact named by the body used to be a
        // detached row handed to a cascade, which also stamped this section's team onto it.
        RequirementArtifact victim = team2Artifact("Data export");

        // Section 16 belongs to team 1's document 1 and is a LIST section. It has to be locked before its content
        // can be updated, and the caller has to hold the lock.
        String lockJson = """
                {
                    "reason": "Locking section for editing"
                }
                """;
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16/lock").contentType(MediaType.APPLICATION_JSON).content(lockJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print());

        MvcResult sectionResult = this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/16").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        int version = new JSONObject(sectionResult.getResponse().getContentAsString()).getJSONObject("data").getInt("version");

        // One list entry, carrying team 2's artifact id and its real artifact key.
        String json = """
                {
                    "id": 16,
                    "sectionKey": "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS",
                    "type": "LIST",
                    "title": "Non-Use Case Functional Requirements",
                    "content": "Updated section content goes here.",
                    "requirementArtifacts": [
                        {
                            "id": %d,
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "artifactKey": "%s",
                            "title": "Stolen",
                            "content": "Stolen content",
                            "priority": "CRITICAL",
                            "sourceSectionId": 16,
                            "notes": ""
                        }
                    ],
                    "version": %d
                }
                """.formatted(victim.getId(), victim.getArtifactKey(), version);

        MvcResult mvcResult = this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString());

        if (response.getBoolean("flag")) {
            assertStillOwnedByTeam2(victim.getId(), "Data export");
        }
    }

    @Test
    @DisplayName("A use-case update naming another team's artifact as a precondition must not overwrite it")
    void testStudentJohnCannotOverwriteAnotherTeamsArtifactThroughUseCaseUpdate() throws Exception {
        // The update path, not the create path. UseCase.preconditions and .postconditions are
        // @ManyToMany(cascade = {MERGE, PERSIST, REFRESH}) and updateUseCase adds the submitted condition
        // artifacts into the loaded use case's collections, so a condition carrying an existing id used to reach
        // a cascaded merge. The converter no longer maps that id, and this probe holds that line.
        //
        // Worth probing separately from the create even though the same converter feeds both: on the peer
        // evaluations the create was refused and the update was the exploitable one, because a managed parent
        // makes the cascade a merge rather than a persist.
        RequirementArtifact victim = team2Artifact("Client");

        // Use case 3 belongs to team 1. It has to be locked by the caller before it can be updated.
        String lockJson = """
                {
                    "reason": "Locking use case for editing"
                }
                """;
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3/lock").contentType(MediaType.APPLICATION_JSON).content(lockJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print());

        MvcResult useCaseResult = this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        JSONObject useCase = new JSONObject(useCaseResult.getResponse().getContentAsString()).getJSONObject("data");
        int version = useCase.getInt("version");
        long primaryActorId = useCase.getLong("primaryActorId");

        // A single precondition, carrying team 2's artifact id.
        String json = """
                {
                    "id": 3,
                    "title": "Create a use case",
                    "description": "The Student wants to create a new use case in the use case document.",
                    "teamId": 1,
                    "primaryActorId": %d,
                    "secondaryActorIds": [],
                    "trigger": "The Student indicates to create a new use case.",
                    "preconditions": [
                        {
                            "id": %d,
                            "condition": "Stolen precondition text",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        }
                    ],
                    "postconditions": [],
                    "mainSteps": [],
                    "version": %d
                }
                """.formatted(primaryActorId, victim.getId(), version);

        MvcResult mvcResult = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString());

        if (response.getBoolean("flag")) {
            assertStillOwnedByTeam2(victim.getId(), "Client");
        }
    }

    @Test
    @DisplayName("A LIST section entry whose artifact key matches nothing becomes a new artifact")
    void testSectionUpdateWithAnUnmatchedArtifactKeyCreatesANewArtifact() throws Exception {
        // The companion to the probe above, which sent team 2's key and happened to collide with a key this
        // section already had, since artifact keys are minted per team. That took the matched branch and left the
        // fall-through untested. This one sends a key belonging to no artifact anywhere, so it exercises the
        // branch the javadoc on mergeSubmittedArtifacts describes: unmatched entries become new rows.
        RequirementArtifact victim = team2Artifact("Data export");
        long artifactCountBefore = this.requirementArtifactRepository.count();

        String lockJson = """
                {
                    "reason": "Locking section for editing"
                }
                """;
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16/lock").contentType(MediaType.APPLICATION_JSON).content(lockJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print());

        MvcResult sectionResult = this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/16").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        int version = new JSONObject(sectionResult.getResponse().getContentAsString()).getJSONObject("data").getInt("version");

        String json = """
                {
                    "id": 16,
                    "sectionKey": "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS",
                    "type": "LIST",
                    "title": "Non-Use Case Functional Requirements",
                    "content": "Updated section content goes here.",
                    "requirementArtifacts": [
                        {
                            "id": %d,
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "artifactKey": "FR-999",
                            "title": "Brand new",
                            "content": "Brand new content",
                            "priority": "CRITICAL",
                            "sourceSectionId": 16,
                            "notes": ""
                        }
                    ],
                    "version": %d
                }
                """.formatted(victim.getId(), version);

        MvcResult mvcResult = this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString());
        assertThat(response.getBoolean("flag")).isTrue();

        // A new row, keyed from team 1's sequence rather than from the "FR-999" the body asked for, and nothing
        // reached the artifact whose id the body carried.
        JSONObject savedArtifact = response.getJSONObject("data").getJSONArray("requirementArtifacts").getJSONObject(0);
        assertThat(savedArtifact.getLong("id")).isNotEqualTo(victim.getId());
        assertThat(savedArtifact.getString("artifactKey")).isNotEqualTo("FR-999");
        assertThat(savedArtifact.getString("title")).isEqualTo("Brand new");
        assertStillOwnedByTeam2(victim.getId(), "Data export");
        assertThat(this.requirementArtifactRepository.count()).isEqualTo(artifactCountBefore + 1);
    }

}
