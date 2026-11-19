package team.projectpulse.ram.requirement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
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
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.DocumentStatus;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.collaboration.CommentRepository;
import team.projectpulse.ram.collaboration.CommentThreadRepository;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.system.StatusCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RequirementArtifactControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentSectionRepository documentSectionRepository;

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    CommentThreadRepository commentThreadRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    EntityManager entityManager;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is on team 1 in section 2

    String studentEricToken; // Eric is on team 1 in section 2

    String studentWoodyToken; // Woody is on team 2 in section 2

    String studentMavisToken; // Mavis is on team 3 in section 2, which has no requirement documents

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("b.wei@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.adminBingyangToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("t.cook@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.smith@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("e.hudson@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentEricToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("w.allen@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentWoodyToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("m.huber@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentMavisToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test

    void findRequirementArtifactsByCriteria1() throws Exception {
        String json = """
                {
                    "type": "FUNCTIONAL_REQUIREMENT"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement artifacts successfully."))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].id").value(9))
                .andExpect(jsonPath("$.data.content[0].title").value("User authentication"))
                .andExpect(jsonPath("$.data.content[0].type").value("FUNCTIONAL_REQUIREMENT"));
    }

    @Test

    void findRequirementArtifactsByCriteria2() throws Exception {
        String json = """
                {
                    "priority": "CRITICAL"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement artifacts successfully."))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(4)));
    }

    @Test

    void findRequirementArtifactsByCriteria3() throws Exception {
        String json = """
                {
                    "content": "user"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement artifacts successfully."))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    /**
     * UC-ART-create-artifact step 5 and extension 5a: the editor's duplicate check is this search with the new item's
     * type and title. A title that is only part of the stored one, in another case, still finds it.
     */
    @Test
    void createArtifact_Extension5a_DuplicateSearchMatchesTypeAndTitleIgnoringCase() throws Exception {
        String json = """
                {
                    "type": "FUNCTIONAL_REQUIREMENT",
                    "title": "AUTHENTICATION"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/search").param("page", "0").param("size", "5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(9))
                .andExpect(jsonPath("$.data.content[0].title").value("User authentication"));

        // The same words under another type are not a duplicate.
        String otherType = """
                {
                    "type": "RISK",
                    "title": "authentication"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/search").contentType(MediaType.APPLICATION_JSON).content(otherType).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void findRequirementArtifactById_Admin() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/requirement-artifacts/9").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement artifact successfully."))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.title").value("User authentication"))
                .andExpect(jsonPath("$.data.type").value("FUNCTIONAL_REQUIREMENT"));
    }

    @Test
    void findRequirementArtifactById_SameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/requirement-artifacts/9").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement artifact successfully."))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.title").value("User authentication"))
                .andExpect(jsonPath("$.data.type").value("FUNCTIONAL_REQUIREMENT"));
    }

    @Test
    void findRequirementArtifactById_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/requirement-artifacts/9").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void addRequirementArtifact_SameTeam() throws Exception {
        String json = """
                {
                    "type": "QUALITY_ATTRIBUTE",
                    "title": "Data encryption",
                    "content": "The system shall encrypt sensitive user data.",
                    "priority": "HIGH",
                    "notes": "Refer to industry standards for encryption."
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add requirement artifact successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Data encryption"))
                .andExpect(jsonPath("$.data.artifactKey").value("QA-1"))
                .andExpect(jsonPath("$.data.type").value("QUALITY_ATTRIBUTE"))
                .andExpect(jsonPath("$.data.homeDocumentType").value("SRS"))
                .andExpect(jsonPath("$.data.version").value(0));
    }

    @Test
    void addRequirementArtifact_NotSameTeam() throws Exception {
        String json = """
                {
                    "type": "QUALITY_ATTRIBUTE",
                    "title": "Data encryption",
                    "content": "The system shall encrypt sensitive user data.",
                    "priority": "HIGH",
                    "notes": "Refer to industry standards for encryption."
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test

    void updateRequirementArtifact() throws Exception {
        int version = artifactVersion(9);
        String json = """
                {
                    "id": 9,
                    "type": "FUNCTIONAL_REQUIREMENT",
                    "title": "New Title: User authentication and authorization",
                    "content": "New Content: The system shall authenticate users and manage their access rights.",
                    "priority": "CRITICAL",
                    "notes": "Update: Include authorization mechanisms.",
                    "version": %d
                }
                """.formatted(version);
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/requirement-artifacts/9").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update requirement artifact successfully"))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.title").value("New Title: User authentication and authorization"))
                .andExpect(jsonPath("$.data.artifactKey").value("FR-1"))
                .andExpect(jsonPath("$.data.type").value("FUNCTIONAL_REQUIREMENT"))
                .andExpect(jsonPath("$.data.version").value(version + 1));
    }

    @Test
    void findRequirementArtifactById_OtherTeamsArtifactThroughOwnTeamUrl() throws Exception {
        // Woody is a student on team 2; requirement artifact 9 belongs to team 1.
        // The URL names team 2, so the membership guard passes and only service-layer
        // scoping can stop this. This isolates the service layer from the route rules.
        this.mockMvc.perform(get(this.baseUrl + "/teams/2/requirement-artifacts/9").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }


    // ---- UC-ART-create-artifact (docs/design/art.md, Tests) ----
    // The seed gives team 1 two documents. Its SRS is document 1: section 16 is NON_USE_CASE_FUNCTIONAL_REQUIREMENTS
    // (FR-1, FR-2, FR-3) and section 13 is ASSUMPTIONS_DEPENDENCIES. Its Vision and Scope is document 2, with no risks,
    // features or business assumptions seeded. Team 2 has documents 3 and 4; team 3 has none.

    private static final long TEAM_1_VISION_SCOPE_ID = 2L;

    @Test
    void createArtifact_MainFlow_FromASection_AppendedWithTheNextKeyAndNoLockNeeded() throws Exception {
        long visionScopeId = TEAM_1_VISION_SCOPE_ID;
        // John holds no document section lock: the server does not check it for items (BR-edit-lock-required).
        JSONObject first = create(1, item("RISK", "Team turnover", "A team member may leave mid-semester."), this.studentJohnToken);
        assertThat(first.getBoolean("flag")).as(first.toString()).isTrue();
        assertThat(first.getJSONObject("data").getString("artifactKey")).isEqualTo("RSK-1");
        assertThat(first.getJSONObject("data").getInt("version")).isZero();
        assertThat(first.getJSONObject("data").getString("homeDocumentType")).isEqualTo("VISION_SCOPE");

        JSONObject second = create(1, item("RISK", "Scope creep", "The client may keep adding features."), this.studentJohnToken);
        assertThat(second.getJSONObject("data").getString("artifactKey")).isEqualTo("RSK-2");

        assertThat(itemKeys(visionScopeId, "RISKS")).containsExactly("RSK-1", "RSK-2");
    }

    @Test
    void createArtifact_MainFlow_FromTheCatalog_HomeFromTheType() throws Exception {
        long visionScopeId = TEAM_1_VISION_SCOPE_ID;
        JSONObject created = create(1, item("FEATURE", "Requirement graph", "Browse every artifact and its links."), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        assertThat(itemKeys(visionScopeId, "MAJOR_FEATURES")).containsExactly(created.getJSONObject("data").getString("artifactKey"));
    }

    @Test
    void createArtifact_AssumptionsAndDependencies_EachKindHasOneHome() throws Exception {
        // The system-level kind goes to the SRS, the business-level kind to the Vision and Scope; neither names a home.
        long visionScopeId = TEAM_1_VISION_SCOPE_ID;
        JSONObject assumption = create(1, item("ASSUMPTION", "Campus SSO stays available", "The university single sign-on remains available."), this.studentJohnToken);
        assertThat(assumption.getBoolean("flag")).as(assumption.toString()).isTrue();
        assertThat(itemKeys(1L, "ASSUMPTIONS_DEPENDENCIES")).containsExactly(assumption.getJSONObject("data").getString("artifactKey"));

        JSONObject businessDependency = create(1, item("BUSINESS_DEPENDENCY", "Course schedule", "The release depends on the semester calendar."), this.studentJohnToken);
        assertThat(businessDependency.getBoolean("flag")).as(businessDependency.toString()).isTrue();
        assertThat(businessDependency.getJSONObject("data").getString("artifactKey")).isEqualTo("BDE-1");
        assertThat(businessDependency.getJSONObject("data").getString("homeDocumentType")).isEqualTo("VISION_SCOPE");
        assertThat(itemKeys(visionScopeId, "ASSUMPTIONS_DEPENDENCIES")).containsExactly("BDE-1");
    }

    @Test
    void createArtifact_TypeWithNoListHome_Refused() throws Exception {
        for (String type : List.of("GLOSSARY_TERM", "USE_CASE", "PRECONDITION", "POSTCONDITION", "USER_STORY")) {
            JSONObject refused = create(1, item(type, "Not here", "Created through its own use case."), this.studentJohnToken);
            assertThat(refused.getBoolean("flag")).as(type).isFalse();
            assertThat(refused.getInt("code")).as(type).isEqualTo(StatusCode.INVALID_ARGUMENT);
            assertThat(refused.getString("message")).as(type).contains("is not created here");
        }
        // The article follows the sound: "a use case", "a user story", and "an" only before a, e, i, o.
        assertThat(create(1, item("USE_CASE", "Not here", "x"), this.studentJohnToken).getString("message")).startsWith("A use case is not created here");
        assertThat(create(1, item("USER_STORY", "Not here", "x"), this.studentJohnToken).getString("message")).startsWith("A user story is not created here");
        assertThat(create(1, item("GLOSSARY_TERM", "Not here", "x"), this.studentJohnToken).getString("message")).startsWith("A glossary term is not created here");
    }

    @Test
    void createArtifact_HomeDocumentTypeInTheBody_Ignored() throws Exception {
        // The type alone names the home: a body naming another document changes nothing.
        long visionScopeId = TEAM_1_VISION_SCOPE_ID;
        JSONObject created = create(1, item("RISK", "Team turnover", "A team member may leave.").put("homeDocumentType", "SRS"), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        assertThat(created.getJSONObject("data").getString("homeDocumentType")).isEqualTo("VISION_SCOPE");
        assertThat(itemKeys(visionScopeId, "RISKS")).containsExactly(created.getJSONObject("data").getString("artifactKey"));
    }

    @Test
    void createArtifact_UnknownEnumValue_Malformed() throws Exception {
        JSONObject refused = create(1, item("OTHER", "Legacy", "The removed fallback type."), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).isEqualTo("The request is malformed.");
    }

    @Test
    void createArtifact_Extension4a_FieldErrors() throws Exception {
        JSONObject refused = create(1, new JSONObject().put("title", "  ").put("content", ""), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        JSONObject errors = refused.getJSONObject("data");
        assertThat(errors.has("type")).as(errors.toString()).isTrue();
        assertThat(errors.has("title")).as(errors.toString()).isTrue();
        assertThat(errors.has("content")).as(errors.toString()).isTrue();

        JSONObject tooLong = create(1, item("FUNCTIONAL_REQUIREMENT", "x".repeat(256), "Content."), this.studentJohnToken);
        assertThat(tooLong.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(tooLong.getJSONObject("data").has("title")).isTrue();
    }

    @Test
    void createArtifact_Extension8a_HomeDocumentLockedForReview_Refused() throws Exception {
        RequirementDocument srs = this.documentRepository.findByTeamTeamIdAndType(1, DocumentType.SRS).orElseThrow();
        srs.setStatus(DocumentStatus.SUBMITTED);
        this.documentRepository.saveAndFlush(srs);

        JSONObject refused = create(1, item("FUNCTIONAL_REQUIREMENT", "Late addition", "The system shall do one more thing."), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).isEqualTo("The Software Requirements Specification is locked for review, so it cannot be changed.");
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2", "FR-3");
    }

    @Test
    void createArtifact_Extension8b_HomeDocumentNotCreated_Refused() throws Exception {
        // Mavis is on team 3, which has no documents.
        JSONObject refused = create(3, item("FUNCTIONAL_REQUIREMENT", "Export", "The system shall export data."), this.studentMavisToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).isEqualTo("The team's Software Requirements Specification has not been created yet. The course admin creates the team's documents.");
    }

    @Test
    void createArtifact_BodyIdIgnored_AnotherTeamsRowUntouched() throws Exception {
        RequirementArtifact team2Fr = this.requirementArtifactRepository.findAll().stream()
                .filter(a -> "Data export".equals(a.getTitle()) && a.getTeam().getTeamId() == 2).findFirst().orElseThrow();
        JSONObject created = create(1, item("FUNCTIONAL_REQUIREMENT", "Overwritten", "Overwritten content.").put("id", team2Fr.getId()).put("artifactKey", "FR-999").put("version", 7), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        JSONObject data = created.getJSONObject("data");
        assertThat(data.getLong("id")).isNotEqualTo(team2Fr.getId());
        assertThat(data.getString("artifactKey")).isEqualTo("FR-4"); // FR-1 to FR-3 are seeded
        assertThat(data.getInt("version")).isZero();
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2", "FR-3", "FR-4");

        this.entityManager.flush();
        this.entityManager.clear();
        RequirementArtifact reread = this.requirementArtifactRepository.findById(team2Fr.getId()).orElseThrow();
        assertThat(reread.getTeam().getTeamId()).isEqualTo(2);
        assertThat(reread.getTitle()).isEqualTo("Data export");
    }

    @Test
    void createArtifact_LockHolderUndisturbed() throws Exception {
        // John holds section 16's lock and is editing its prose; Eric adds an item to the same list meanwhile.
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16/lock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Editing\"}").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true));
        int version = sectionVersion(16);

        JSONObject created = create(1, item("FUNCTIONAL_REQUIREMENT", "Audit log", "The system shall log every change."), this.studentEricToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        this.entityManager.flush();
        assertThat(sectionVersion(16)).as("a create leaves the document section's version alone").isEqualTo(version);

        JSONObject body = new JSONObject().put("type", "LIST").put("content", "John's prose.").put("version", version);
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        JSONObject saved = new JSONObject(result.getResponse().getContentAsString());
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        assertThat(saved.getJSONObject("data").getJSONArray("requirementArtifacts").length()).isEqualTo(4);
    }

    @Test
    void createArtifact_Instructor_OfTheTeamsCourseSectionAllowed_OfAnotherRefused() throws Exception {
        JSONObject created = create(1, item("FUNCTIONAL_REQUIREMENT", "Instructor example", "The system shall show an example."), this.adminBingyangToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();

        JSONObject refused = create(1, item("FUNCTIONAL_REQUIREMENT", "Not mine", "The system shall do something."), this.adminTimToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.FORBIDDEN);
    }

    // ---- UC-ART-edit-artifact, as far as the cutover needs (docs/design/art.md, Tests) ----

    @Test
    void updateArtifact_Extension10a_StaleVersionConflicts_MissingVersionRefused() throws Exception {
        int version = artifactVersion(9);
        JSONObject first = update(9, item("FUNCTIONAL_REQUIREMENT", "User sign-in", "The system shall let users sign in.").put("version", version), this.studentJohnToken);
        assertThat(first.getBoolean("flag")).as(first.toString()).isTrue();
        assertThat(first.getJSONObject("data").getInt("version")).isEqualTo(version + 1);

        JSONObject stale = update(9, item("FUNCTIONAL_REQUIREMENT", "Stale edit", "Written from an old copy.").put("version", version), this.studentEricToken);
        assertThat(stale.getBoolean("flag")).isFalse();
        assertThat(stale.getInt("code")).isEqualTo(StatusCode.CONFLICT);

        JSONObject noVersion = update(9, item("FUNCTIONAL_REQUIREMENT", "No version", "Sent without one."), this.studentJohnToken);
        assertThat(noVersion.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
    }

    @Test
    void updateArtifact_HomeDocumentLockedForReview_Refused() throws Exception {
        RequirementDocument srs = this.documentRepository.findByTeamTeamIdAndType(1, DocumentType.SRS).orElseThrow();
        srs.setStatus(DocumentStatus.SUBMITTED);
        this.documentRepository.saveAndFlush(srs);

        JSONObject refused = update(9, item("FUNCTIONAL_REQUIREMENT", "Changed under review", "Should not save.").put("version", artifactVersion(9)), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).contains("locked for review");
    }

    @Test
    void updateArtifact_OtherTeamsArtifactThroughOwnTeamUrl_NotFound() throws Exception {
        // Woody is on team 2 and names team 2 in the URL, so the route rule passes; artifact 9 is team 1's.
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/2/requirement-artifacts/9").contentType(MediaType.APPLICATION_JSON)
                .content(item("FUNCTIONAL_REQUIREMENT", "Taken", "Not mine.").put("version", 0).toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken)).andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertThat(response.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
    }

    @Test
    void updateArtifact_TypeWithNoListHome_Refused() throws Exception {
        // The generic PUT would otherwise bypass the use case lock, the shared-condition wording rule and the glossary.
        for (RequirementArtifactType type : List.of(RequirementArtifactType.GLOSSARY_TERM, RequirementArtifactType.USE_CASE, RequirementArtifactType.PRECONDITION)) {
            RequirementArtifact target = this.requirementArtifactRepository.findAll().stream()
                    .filter(a -> a.getType() == type && a.getTeam().getTeamId() == 1).findFirst()
                    .orElseThrow(() -> new AssertionError("No team 1 " + type + " in the seed data"));
            JSONObject refused = update(target.getId(), item(type.name(), "Renamed here", "Should not save.").put("version", target.getVersion()), this.studentJohnToken);
            assertThat(refused.getInt("code")).as(type.name()).isEqualTo(StatusCode.INVALID_ARGUMENT);
            assertThat(refused.getString("message")).as(type.name()).contains("is not edited here");
        }
    }

    // ---- UC-ART-delete-artifact (docs/design/art.md, Tests) ----
    // Team 1's seed: FR-1 and FR-2 each derive from UC-1, BO-1 is the target of UC-1's link, stakeholders 1 and 2 are
    // UC-1's primary and secondary actors. FR-3 and the external system are referenced by nothing.

    @Test
    void deleteArtifact_MainFlow_RowGoneAndListClosesUp() throws Exception {
        long fr3 = idOf(1, "FR-3");
        JSONObject deleted = deleteArtifact(1, fr3, artifactVersion(fr3), this.studentJohnToken);
        assertThat(deleted.getBoolean("flag")).as(deleted.toString()).isTrue();
        assertThat(deleted.getString("message")).isEqualTo("Delete requirement artifact successfully");

        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(this.requirementArtifactRepository.findById(fr3)).isEmpty();
        MvcResult read = this.mockMvc.perform(get(this.baseUrl + "/teams/1/requirement-artifacts/" + fr3)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        assertThat(new JSONObject(read.getResponse().getContentAsString()).getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2");
    }

    @Test
    void deleteArtifact_Extension4a_ReferencedArtifactRefused_ReferrersNamed() throws Exception {
        long fr1 = idOf(1, "FR-1");
        JSONObject refused = deleteArtifact(1, fr1, artifactVersion(fr1), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).isEqualTo("FR-1 cannot be deleted while other artifacts refer to it: the link FR-1 DERIVES_FROM UC-1. Remove or repoint these references first.");
        JSONObject referrer = refused.getJSONArray("data").getJSONObject(0);
        assertThat(referrer.getString("artifactKey")).isEqualTo("UC-1");
        assertThat(referrer.getString("relation")).isEqualTo("LINK_FROM");
        assertThat(referrer.getString("linkType")).isEqualTo("DERIVES_FROM");

        long stakeholder = idOf(1, "STK-1");
        JSONObject actorRefused = deleteArtifact(1, stakeholder, artifactVersion(stakeholder), this.studentJohnToken);
        assertThat(actorRefused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(actorRefused.getString("message")).contains("UC-1 names it as its primary actor");

        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(this.requirementArtifactRepository.findById(fr1)).isPresent();
        assertThat(this.requirementArtifactRepository.findById(stakeholder)).isPresent();
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2", "FR-3");
    }

    @Test
    void deleteArtifact_Extension7a_StaleVersionConflicts_MissingVersionRefused() throws Exception {
        long fr3 = idOf(1, "FR-3");
        int seen = artifactVersion(fr3);
        JSONObject edited = update(fr3, item("FUNCTIONAL_REQUIREMENT", "Notification", "The system shall notify teammates by email.").put("version", seen), this.studentEricToken);
        assertThat(edited.getBoolean("flag")).as(edited.toString()).isTrue();
        this.entityManager.flush();

        JSONObject stale = deleteArtifact(1, fr3, seen, this.studentJohnToken);
        assertThat(stale.getInt("code")).isEqualTo(StatusCode.CONFLICT);

        JSONObject noVersion = deleteArtifact(1, fr3, null, this.studentJohnToken);
        assertThat(noVersion.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(noVersion.getString("message")).isEqualTo("The requirement artifact's version is required to delete it.");
        assertThat(this.requirementArtifactRepository.findById(fr3)).isPresent();
    }

    @Test
    void deleteArtifact_Precondition3_HomeDocumentLockedForReview_Refused() throws Exception {
        RequirementDocument srs = this.documentRepository.findByTeamTeamIdAndType(1, DocumentType.SRS).orElseThrow();
        srs.setStatus(DocumentStatus.SUBMITTED);
        this.documentRepository.saveAndFlush(srs);

        long fr3 = idOf(1, "FR-3");
        JSONObject refused = deleteArtifact(1, fr3, artifactVersion(fr3), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).isEqualTo("The Software Requirements Specification is locked for review, so it cannot be changed.");
        assertThat(this.requirementArtifactRepository.findById(fr3)).isPresent();
    }

    @Test
    void deleteArtifact_TypeWithNoListHome_Refused() throws Exception {
        for (RequirementArtifactType type : List.of(RequirementArtifactType.GLOSSARY_TERM, RequirementArtifactType.USE_CASE, RequirementArtifactType.PRECONDITION)) {
            RequirementArtifact target = this.requirementArtifactRepository.findAll().stream()
                    .filter(a -> a.getType() == type && a.getTeam().getTeamId() == 1).findFirst()
                    .orElseThrow(() -> new AssertionError("No team 1 " + type + " in the seed data"));
            JSONObject refused = deleteArtifact(1, target.getId(), target.getVersion(), this.studentJohnToken);
            assertThat(refused.getInt("code")).as(type.name()).isEqualTo(StatusCode.INVALID_ARGUMENT);
            assertThat(refused.getString("message")).as(type.name()).contains("is not deleted here");
            assertThat(this.requirementArtifactRepository.findById(target.getId())).as(type.name()).isPresent();
        }
    }

    @Test
    void deleteArtifact_CrossTeam_NonMemberForbidden_OtherTeamsIdNotFound() throws Exception {
        long fr3 = idOf(1, "FR-3");
        // Woody is on team 2: team 1's URL is refused at the route, and team 1's id through team 2's URL is not found.
        assertThat(deleteArtifact(1, fr3, 0, this.studentWoodyToken).getInt("code")).isEqualTo(StatusCode.FORBIDDEN);
        assertThat(deleteArtifact(2, fr3, 0, this.studentWoodyToken).getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(this.requirementArtifactRepository.findById(fr3)).isPresent();
    }

    @Test
    void deleteArtifact_CommentThreadsGoWithIt() throws Exception {
        JSONObject created = create(1, item("FUNCTIONAL_REQUIREMENT", "Audit log", "The system shall log every change."), this.studentJohnToken);
        long id = created.getJSONObject("data").getLong("id");
        MvcResult thread = this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/" + id + "/comment-threads")
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\": \"Which changes count?\"}")
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken)).andReturn();
        long threadId = new JSONObject(thread.getResponse().getContentAsString()).getJSONObject("data").getLong("id");
        // Clear as a request boundary would: CommentService sets the thread's artifact but does not add the thread to
        // the artifact's in-memory set, so in this one test transaction the cascade would not see it.
        this.entityManager.flush();
        this.entityManager.clear();

        JSONObject deleted = deleteArtifact(1, id, artifactVersion(id), this.studentJohnToken);
        assertThat(deleted.getBoolean("flag")).as(deleted.toString()).isTrue();
        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(this.commentThreadRepository.findById(threadId)).isEmpty();
        assertThat(this.commentRepository.findAll()).noneMatch(comment -> comment.getCommentThread().getId() == threadId);
    }

    @Test
    void deleteArtifact_Instructor_OfTheTeamsCourseSectionAllowed_OfAnotherRefused() throws Exception {
        long fr3 = idOf(1, "FR-3");
        assertThat(deleteArtifact(1, fr3, artifactVersion(fr3), this.adminTimToken).getInt("code")).isEqualTo(StatusCode.FORBIDDEN);
        JSONObject deleted = deleteArtifact(1, fr3, artifactVersion(fr3), this.adminBingyangToken);
        assertThat(deleted.getBoolean("flag")).as(deleted.toString()).isTrue();
    }

    @Test
    void deleteArtifact_KeyNotReused() throws Exception {
        long fr3 = idOf(1, "FR-3");
        assertThat(deleteArtifact(1, fr3, artifactVersion(fr3), this.studentJohnToken).getBoolean("flag")).isTrue();
        JSONObject next = create(1, item("FUNCTIONAL_REQUIREMENT", "Audit log", "The system shall log every change."), this.studentJohnToken);
        assertThat(next.getJSONObject("data").getString("artifactKey")).isEqualTo("FR-4");
    }

    @Test
    void deleteArtifact_ItemInAList_LeavesNoGap() throws Exception {
        // Section 16 holds FR-1 to FR-3; a create appends FR-4, then FR-3 is deleted from between FR-2 and FR-4.
        JSONObject created = create(1, item("FUNCTIONAL_REQUIREMENT", "Audit log", "The system shall log every change."), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        long fr3 = idOf(1, "FR-3");
        assertThat(deleteArtifact(1, fr3, artifactVersion(fr3), this.studentJohnToken).getBoolean("flag")).isTrue();

        // Reload from the database, past this test's persistence context, so the stored indexes are what is read.
        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2", "FR-4");
    }

    @Test
    void deleteArtifact_LockHolderUndisturbed() throws Exception {
        // John holds section 16's lock and is editing its prose; Eric deletes an item of the same list meanwhile.
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16/lock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Editing\"}").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true));
        int version = sectionVersion(16);

        long fr3 = idOf(1, "FR-3");
        assertThat(deleteArtifact(1, fr3, artifactVersion(fr3), this.studentEricToken).getBoolean("flag")).isTrue();
        this.entityManager.flush();
        assertThat(sectionVersion(16)).as("a delete leaves the document section's version alone").isEqualTo(version);

        JSONObject body = new JSONObject().put("type", "LIST").put("content", "John's prose.").put("version", version);
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        assertThat(new JSONObject(result.getResponse().getContentAsString()).getBoolean("flag")).isTrue();
    }

    @Test
    void deleteArtifact_NoLinkCascade_ForeignKeyRefusesADeletePastTheCheck() {
        // Straight to the repository, past the service's reference check: without a cascade, the link's foreign key
        // refuses the delete instead of the link being deleted with the artifact.
        RequirementArtifact fr1 = this.requirementArtifactRepository.findById(idOf(1, "FR-1")).orElseThrow();
        assertThatThrownBy(() -> {
            this.requirementArtifactRepository.delete(fr1);
            this.entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    private JSONObject deleteArtifact(int teamId, long artifactId, Integer version, String token) throws Exception {
        String url = this.baseUrl + "/teams/" + teamId + "/requirement-artifacts/" + artifactId + (version == null ? "" : "?version=" + version);
        MvcResult result = this.mockMvc.perform(delete(url).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    /** The id of team {@code teamId}'s artifact with key {@code artifactKey}, such as "FR-3". */
    private long idOf(int teamId, String artifactKey) {
        return this.requirementArtifactRepository.findAll().stream()
                .filter(a -> a.getTeam().getTeamId() == teamId && artifactKey.equals(a.getArtifactKey()))
                .findFirst().orElseThrow(() -> new AssertionError("No " + artifactKey + " for team " + teamId)).getId();
    }

    private static JSONObject item(String type, String title, String content) throws Exception {
        return new JSONObject().put("type", type).put("title", title).put("content", content).put("priority", "MEDIUM").put("notes", "");
    }

    private JSONObject create(int teamId, JSONObject body, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/" + teamId + "/requirement-artifacts").contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    private JSONObject update(long artifactId, JSONObject body, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/requirement-artifacts/" + artifactId).contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    /** The artifact keys of a document's list section, in list order, as the document section GET serves them. */
    private List<String> itemKeys(long documentId, String sectionKey) throws Exception {
        long sectionId = this.documentSectionRepository.findByDocumentIdAndSectionKey(documentId, sectionKey).orElseThrow().getId();
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/" + documentId + "/document-sections/" + sectionId)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        JSONArray items = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONArray("requirementArtifacts");
        java.util.List<String> keys = new java.util.ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            keys.add(items.getJSONObject(i).getString("artifactKey"));
        }
        return keys;
    }

    private int artifactVersion(long artifactId) throws Exception {
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/1/requirement-artifacts/" + artifactId)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getInt("version");
    }

    private int sectionVersion(long sectionId) throws Exception {
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/" + sectionId)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getInt("version");
    }

}
