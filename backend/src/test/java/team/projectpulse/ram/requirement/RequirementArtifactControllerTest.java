package team.projectpulse.ram.requirement;

import jakarta.persistence.EntityManager;
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
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.system.StatusCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    EntityManager entityManager;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is on team 1 in section 2

    String studentEricToken; // Eric is on team 1 in section 2

    String studentWoodyToken; // Woody is on team 2 in section 2

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
    // The seed gives team 1 one document, its SRS (document 1): section 16 is NON_USE_CASE_FUNCTIONAL_REQUIREMENTS
    // (FR-1, FR-2) and section 13 is ASSUMPTIONS_DEPENDENCIES. Team 2 has no document. A test that needs team 1's Vision
    // and Scope creates it first, inside the test's rolled-back transaction.

    @Test
    void createArtifact_MainFlow_FromASection_AppendedWithTheNextKeyAndNoLockNeeded() throws Exception {
        long visionScopeId = createVisionScopeForTeam1();
        // John holds no document section lock: the server does not check it for items (BR-edit-lock-required).
        JSONObject first = create(1, item("RISK", "Team turnover", "A team member may leave mid-semester.").put("homeDocumentType", "VISION_SCOPE"), this.studentJohnToken);
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
        long visionScopeId = createVisionScopeForTeam1();
        JSONObject created = create(1, item("FEATURE", "Requirement graph", "Browse every artifact and its links."), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        assertThat(itemKeys(visionScopeId, "MAJOR_FEATURES")).containsExactly(created.getJSONObject("data").getString("artifactKey"));
    }

    @Test
    void createArtifact_TwoHomeType_GoesToTheNamedHome_AndNeedsOne() throws Exception {
        JSONObject created = create(1, item("ASSUMPTION", "Campus SSO stays available", "The university single sign-on remains available.").put("homeDocumentType", "SRS"), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        assertThat(itemKeys(1L, "ASSUMPTIONS_DEPENDENCIES")).containsExactly(created.getJSONObject("data").getString("artifactKey"));

        JSONObject refused = create(1, item("DEPENDENCY", "Email relay", "Notification emails depend on the campus SMTP relay."), this.studentJohnToken);
        assertThat(refused.getBoolean("flag")).isFalse();
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).startsWith("Choose whether this dependency belongs in the");
    }

    @Test
    void createArtifact_TypeWithNoListHome_Refused() throws Exception {
        for (String type : List.of("GLOSSARY_TERM", "USE_CASE", "PRECONDITION", "POSTCONDITION", "USER_STORY")) {
            JSONObject refused = create(1, item(type, "Not here", "Created through its own use case."), this.studentJohnToken);
            assertThat(refused.getBoolean("flag")).as(type).isFalse();
            assertThat(refused.getInt("code")).as(type).isEqualTo(StatusCode.INVALID_ARGUMENT);
            assertThat(refused.getString("message")).as(type).contains("is not created here");
        }
    }

    @Test
    void createArtifact_WrongHome_Refused() throws Exception {
        JSONObject refused = create(1, item("RISK", "Team turnover", "A team member may leave.").put("homeDocumentType", "SRS"), this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).isEqualTo("A risk belongs in the Vision and Scope Document, not the Software Requirements Specification.");
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
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2");
    }

    @Test
    void createArtifact_Extension8b_HomeDocumentNotCreated_Refused() throws Exception {
        // Woody is on team 2, which has no documents.
        JSONObject refused = create(2, item("FUNCTIONAL_REQUIREMENT", "Export", "The system shall export data."), this.studentWoodyToken);
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
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-2", "FR-4");

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
        assertThat(saved.getJSONObject("data").getJSONArray("requirementArtifacts").length()).isEqualTo(3);
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

    @Test
    void deleteArtifact_ItemInAList_LeavesNoGap() throws Exception {
        // Section 16 holds FR-1 and FR-2; a create appends FR-4, then FR-2 (artifact 10) is deleted from the middle.
        JSONObject created = create(1, item("FUNCTIONAL_REQUIREMENT", "Audit log", "The system shall log every change."), this.studentJohnToken);
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        MvcResult deleted = this.mockMvc.perform(delete(this.baseUrl + "/teams/1/requirement-artifacts/10")
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        assertThat(new JSONObject(deleted.getResponse().getContentAsString()).getBoolean("flag")).isTrue();

        // Reload from the database, past this test's persistence context, so the stored indexes are what is read.
        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(itemKeys(1L, "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS")).containsExactly("FR-1", "FR-4");
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

    /** Creates team 1's Vision and Scope document through the documents endpoint and returns its id. */
    private long createVisionScopeForTeam1() throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents").contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"VISION_SCOPE\"}").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken)).andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        return response.getJSONObject("data").getLong("id");
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
