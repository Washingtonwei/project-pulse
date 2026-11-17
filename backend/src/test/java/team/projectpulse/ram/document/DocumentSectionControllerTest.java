package team.projectpulse.ram.document;

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
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.system.StatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class DocumentSectionControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DocumentSectionRepository documentSectionRepository;

    @Autowired
    RequirementArtifactService requirementArtifactService;

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
    void findDocumentSectionById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find document section successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sectionKey").value("INTRODUCTION"))
                .andExpect(jsonPath("$.data.type").value("RICH_TEXT"))
                .andExpect(jsonPath("$.data.title").value("Introduction"));
    }

    @Test
    void findDocumentSectionById_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test

    void updateDocumentSectionContent1() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Lock document section successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.documentSectionId").value(16))
                .andExpect(jsonPath("$.data.reason").value("Locking section for editing"));

        int version = fetchSectionVersion(1, 1, 16, this.adminBingyangToken);
        json = """
                {
                    "id": 16,
                    "sectionKey": "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS",
                    "type": "LIST",
                    "title": "Non-Use Case Functional Requirements",
                    "content": "Updated section content goes here.",
                    "requirementArtifacts": [
                        {
                            "id": 9,
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "artifactKey": "FR-1",
                            "title": "User authentication",
                            "content": "The system shall allow users to log in using their email and password.",
                            "priority": "CRITICAL",
                            "sourceSectionId": 16,
                            "notes": ""
                        },
                        {
                            "id": 10,
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "artifactKey": "FR-2",
                            "title": "Validation",
                            "content": "The system shall validate user inputs to ensure data integrity and prevent errors.",
                            "priority": "CRITICAL",
                            "sourceSectionId": 16,
                            "notes": ""
                        },
                        {
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "title": "Exclusive Locking Mechanism",
                            "content": "When a student begins editing a section, the system shall acquire an exclusive lock for that section for that student.",
                            "priority": "HIGH",
                            "sourceSectionId": 16,
                            "notes": "This requirement is crucial to prevent concurrent edits."
                        }
                    ],
                    "guidance": "Describe functional requirements that are not easily expressed as use cases.\\n\\nUse structured formats such as:\\n- \\"The system shall…\\" statements\\n- EARS (Easy Approach to Requirements Syntax)\\n",
                    "version": %d
                }
                """.formatted(version);

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update document section content successfully"))
                .andExpect(jsonPath("$.data.id").value(16))
                .andExpect(jsonPath("$.data.content").value("Updated section content goes here."))
                .andExpect(jsonPath("$.data.requirementArtifacts", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data.requirementArtifacts[0].artifactKey").value("FR-1"));
    }

    @Test

    void updateDocumentSectionContent2() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Lock document section successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.documentSectionId").value(16))
                .andExpect(jsonPath("$.data.reason").value("Locking section for editing"));

        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);
        json = """
                {
                    "id": 16,
                    "sectionKey": "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS",
                    "type": "LIST",
                    "title": "Non-Use Case Functional Requirements",
                    "content": "Updated section content goes here.",
                    "requirementArtifacts": [
                        {
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "title": "Exclusive Locking Mechanism",
                            "content": "When a student begins editing a section, the system shall acquire an exclusive lock for that section for that student.",
                            "priority": "HIGH",
                            "sourceSectionId": 16,
                            "notes": "This requirement is crucial to prevent concurrent edits."
                        }
                    ],
                    "guidance": "Describe functional requirements that are not easily expressed as use cases.\\n\\nUse structured formats such as:\\n- \\"The system shall…\\" statements\\n- EARS (Easy Approach to Requirements Syntax)\\n",
                    "version": %d
                }
                """.formatted(version);

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update document section content successfully"))
                .andExpect(jsonPath("$.data.id").value(16))
                .andExpect(jsonPath("$.data.content").value("Updated section content goes here."))
                .andExpect(jsonPath("$.data.requirementArtifacts", Matchers.hasSize(1)));
    }

    @Test

    void updateDocumentSectionWithoutRequirementsArtifacts() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Lock document section successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.documentSectionId").value(1))
                .andExpect(jsonPath("$.data.reason").value("Locking section for editing"));

        int version = fetchSectionVersion(1, 1, 1, this.studentJohnToken);
        json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here.",
                    "version": %d
                }
                """.formatted(version);

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update document section content successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("Updated section content goes here."));
    }

    @Test

    void updateDocumentSectionWithoutRequirementsArtifacts_LockedByAnotherUser() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Lock document section successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.documentSectionId").value(1))
                .andExpect(jsonPath("$.data.reason").value("Locking section for editing"));

        int version = fetchSectionVersion(1, 1, 1, this.studentJohnToken);
        json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here.",
                    "version": %d
                }
                """.formatted(version);

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.LOCKED))
                .andExpect(jsonPath("$.message").value("Document section is locked by another user. You cannot update its content."));
    }

    @Test

    void updateDocumentSectionWithoutRequirementsArtifacts_NotLocked() throws Exception {
        int version = fetchSectionVersion(1, 1, 2, this.studentJohnToken);
        String json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here.",
                    "version": %d
                }
                """.formatted(version);

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.CONFLICT))
                .andExpect(jsonPath("$.message").value("You must first lock this document section before updating its content."));
    }

    @Test
    void updateDocumentSectionWithoutRequirementsArtifacts_NotSameTeam() throws Exception {
        String json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here."
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void getDocumentSectionLockStatus() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find lock status successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.documentSectionId").value(1));
    }

    @Test
    void getDocumentSectionLockStatus_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void lockDocumentSection() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/2/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Lock document section successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.documentSectionId").value(2))
                .andExpect(jsonPath("$.data.reason").value("Locking section for editing"));
    }

    @Test
    void lockDocumentSection_NotSameTeam() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/2/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void lockDocumentSection_AlreadyLocked() throws Exception {
        String json = """
                {
                    "reason": "Locking section for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.LOCKED));
    }

    @Test

    void unlockDocumentSection() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Unlock document section successfully"));
    }

    @Test
    void unlockDocumentSection_DifferentOwner() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Only the lock owner or an instructor can unlock this section."));
    }

    @Test

    void unlockDocumentSection_Instructor() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Unlock document section successfully"));
    }

    private int fetchSectionVersion(int teamId, int documentId, int documentSectionId, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(
                        get(this.baseUrl + "/teams/" + teamId + "/documents/" + documentId + "/document-sections/" + documentSectionId)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                )
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject json = new JSONObject(content);
        return json.getJSONObject("data").getInt("version");
    }


    @Test
    void findDocumentSectionById_OtherTeamsSectionThroughOwnTeamUrl() throws Exception {
        // Woody is a student on team 2; document 1 and section 1 belong to team 1. The URL
        // names team 2, so the membership guard passes and only the scoped query stops this.
        this.mockMvc.perform(get(this.baseUrl + "/teams/2/documents/1/document-sections/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }


    @Test
    void getDocumentSectionLockStatus_OtherTeamsSectionThroughOwnTeamUrl() throws Exception {
        // Woody is a student on team 2; document 1 and section 1 belong to team 1. The lock
        // lookup used to load the section by primary key, leaking another team's lock state.
        this.mockMvc.perform(get(this.baseUrl + "/teams/2/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }


    // ---- UC-ART-create-artifact: adding items in a list-type document section (docs/design/art.md, Tests) ----
    // The seed has one document, team 1's SRS (document 1). Section 16 is NON_USE_CASE_FUNCTIONAL_REQUIREMENTS, which
    // admits FUNCTIONAL_REQUIREMENT and holds FR-1 and FR-2; section 13 is ASSUMPTIONS_DEPENDENCIES, which admits two
    // types; section 1 is the RICH_TEXT INTRODUCTION.

    @Test
    void createArtifact_MainFlow_NewItemGetsKeyAndListOnlySaveBumpsVersion() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(newItem("FUNCTIONAL_REQUIREMENT", "Exclusive locking", "The system shall lock a document section for one student at a time."));
        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);

        // Content is left unchanged (null, as seeded), so only the list changes.
        JSONObject saved = putSection(16, version, items, this.studentJohnToken);
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        JSONObject data = saved.getJSONObject("data");
        JSONArray savedItems = data.getJSONArray("requirementArtifacts");
        assertThat(savedItems.length()).isEqualTo(3);
        assertThat(savedItems.getJSONObject(0).getString("artifactKey")).isEqualTo("FR-1");
        assertThat(savedItems.getJSONObject(1).getString("artifactKey")).isEqualTo("FR-2");
        // FR-1 to FR-3 are seeded, so the new item takes the next key of its type, and keeps its position.
        assertThat(savedItems.getJSONObject(2).getString("artifactKey")).isEqualTo("FR-4");
        assertThat(savedItems.getJSONObject(2).getString("title")).isEqualTo("Exclusive locking");
        assertThat(data.getInt("version")).as("a list-only save increments the version").isGreaterThan(version);

        // A second save from the stale version is refused.
        JSONObject stale = putSection(16, version, items, this.studentJohnToken);
        assertThat(stale.getBoolean("flag")).isFalse();
        assertThat(stale.getInt("code")).isEqualTo(StatusCode.CONFLICT);

        // The artifact search finds the new item (step 5's duplicate check reads the same search).
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/search").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"FUNCTIONAL_REQUIREMENT\", \"title\": \"Exclusive locking\"}").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content[0].artifactKey").value("FR-4"));
    }

    @Test
    void createArtifact_SectionWithTwoTypes_AcceptsBoth() throws Exception {
        lockSectionAs(13, this.studentJohnToken);
        JSONArray items = new JSONArray()
                .put(newItem("ASSUMPTION", "Campus SSO stays available", "The university single sign-on service remains available."))
                .put(newItem("DEPENDENCY", "Email relay", "Notification emails depend on the campus SMTP relay."));

        JSONObject saved = putSection(13, fetchSectionVersion(1, 1, 13, this.studentJohnToken), items, this.studentJohnToken);
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        JSONArray savedItems = saved.getJSONObject("data").getJSONArray("requirementArtifacts");
        assertThat(savedItems.getJSONObject(0).getString("type")).isEqualTo("ASSUMPTION");
        assertThat(savedItems.getJSONObject(0).getString("artifactKey")).startsWith("ASM-");
        assertThat(savedItems.getJSONObject(1).getString("type")).isEqualTo("DEPENDENCY");
        assertThat(savedItems.getJSONObject(1).getString("artifactKey")).startsWith("DEP-");
    }

    @Test
    void createArtifact_TypeNotAdmitted_RefusedAndNothingStored() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(newItem("RISK", "Team turnover", "A team member may leave mid-semester."));
        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);

        JSONObject refused = putSection(16, version, items, this.studentJohnToken);
        assertThat(refused.getBoolean("flag")).isFalse();
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).contains("RISK").contains("Non-Use Case Functional Requirements");
        assertThat(currentItems(16, this.studentJohnToken).length()).as("nothing stored").isEqualTo(2);
        assertThat(fetchSectionVersion(1, 1, 16, this.studentJohnToken)).as("version unchanged").isEqualTo(version);
    }

    @Test
    void createArtifact_Extension4a_MissingFieldsAreFieldErrors() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(new JSONObject().put("title", "").put("content", ""));
        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);

        JSONObject refused = putSection(16, version, items, this.studentJohnToken);
        assertThat(refused.getBoolean("flag")).isFalse();
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        JSONObject errors = refused.getJSONObject("data");
        assertThat(errors.has("requirementArtifacts[2].title")).as(errors.toString()).isTrue();
        assertThat(errors.has("requirementArtifacts[2].content")).as(errors.toString()).isTrue();
        assertThat(errors.has("requirementArtifacts[2].type")).as(errors.toString()).isTrue();
    }

    @Test
    void createArtifact_Extension4a_BlankTitleOrContentIsAFieldError() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(newItem("FUNCTIONAL_REQUIREMENT", "   ", "  	 "));
        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);

        JSONObject refused = putSection(16, version, items, this.studentJohnToken);
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        JSONObject errors = refused.getJSONObject("data");
        assertThat(errors.has("requirementArtifacts[2].title")).as(errors.toString()).isTrue();
        assertThat(errors.has("requirementArtifacts[2].content")).as(errors.toString()).isTrue();
        assertThat(currentItems(16, this.studentJohnToken).length()).isEqualTo(2);
    }

    @Test
    void createArtifact_ExistingItemOfTypeNotAdmitted_SavesUnchanged() throws Exception {
        // A legacy OTHER row, as the old editor created by default. Written through the service, as old data was.
        DocumentSection section = this.documentSectionRepository.findById(16L).orElseThrow();
        RequirementArtifact legacy = this.requirementArtifactService.saveRequirementArtifact(1,
                new RequirementArtifact(section.getDocument().getTeam(), RequirementArtifactType.OTHER, "Legacy item", "Created before admitted types.", ""));
        section.addRequirementArtifact(legacy);
        this.documentSectionRepository.save(section);

        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        JSONObject legacyItem = items.getJSONObject(2);
        legacyItem.put("title", "Legacy item, retitled");

        JSONObject saved = putSection(16, fetchSectionVersion(1, 1, 16, this.studentJohnToken), items, this.studentJohnToken);
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        JSONObject savedLegacy = saved.getJSONObject("data").getJSONArray("requirementArtifacts").getJSONObject(2);
        assertThat(savedLegacy.getLong("id")).isEqualTo(legacy.getId());
        assertThat(savedLegacy.getString("type")).isEqualTo("OTHER");
        assertThat(savedLegacy.getString("artifactKey")).isEqualTo(legacy.getArtifactKey());
        assertThat(savedLegacy.getString("title")).isEqualTo("Legacy item, retitled");
    }

    @Test
    void createArtifact_RepeatedId_Refused() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(new JSONObject(items.getJSONObject(0).toString()));
        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);

        JSONObject refused = putSection(16, version, items, this.studentJohnToken);
        assertThat(refused.getBoolean("flag")).isFalse();
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).contains("Item 3");
        assertThat(currentItems(16, this.studentJohnToken).length()).as("nothing written").isEqualTo(2);
        assertThat(fetchSectionVersion(1, 1, 16, this.studentJohnToken)).isEqualTo(version);
    }

    @Test
    void createArtifact_ValidationOrder_InvalidItemBeforeStaleVersion() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(new JSONObject().put("type", "FUNCTIONAL_REQUIREMENT").put("title", "").put("content", "x"));
        int staleVersion = fetchSectionVersion(1, 1, 16, this.studentJohnToken) - 1;

        JSONObject refused = putSection(16, staleVersion, items, this.studentJohnToken);
        assertThat(refused.getInt("code")).as(refused.toString()).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getJSONObject("data").has("requirementArtifacts[2].title")).isTrue();
    }

    @Test
    void createArtifact_ValidationOrder_TypeNotAdmittedBeforeStaleVersion() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.put(newItem("RISK", "Team turnover", "A team member may leave mid-semester."));
        int staleVersion = fetchSectionVersion(1, 1, 16, this.studentJohnToken) - 1;

        JSONObject refused = putSection(16, staleVersion, items, this.studentJohnToken);
        assertThat(refused.getInt("code")).as(refused.toString()).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.getString("message")).contains("RISK");
        assertThat(currentItems(16, this.studentJohnToken).length()).as("nothing written").isEqualTo(2);
    }

    @Test
    void createArtifact_InstructorOfTheTeamsCourseSection_CanAddAnItem() throws Exception {
        lockSectionAs(16, this.adminBingyangToken);
        JSONArray items = currentItems(16, this.adminBingyangToken);
        items.put(newItem("FUNCTIONAL_REQUIREMENT", "Instructor example", "The system shall show an example requirement."));

        JSONObject saved = putSection(16, fetchSectionVersion(1, 1, 16, this.adminBingyangToken), items, this.adminBingyangToken);
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        assertThat(saved.getJSONObject("data").getJSONArray("requirementArtifacts").length()).isEqualTo(3);
    }

    @Test
    void createArtifact_InstructorOfAnotherCourseSection_Refused() throws Exception {
        JSONArray items = new JSONArray().put(newItem("FUNCTIONAL_REQUIREMENT", "Not mine", "The system shall do something."));
        JSONObject refused = putSection(16, 0, items, this.adminTimToken);
        assertThat(refused.getBoolean("flag")).isFalse();
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.FORBIDDEN);
    }

    @Test
    void findDocumentSectionById_ReturnsAdmittedTypes() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/13").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.data.sectionKey").value("ASSUMPTIONS_DEPENDENCIES"))
                .andExpect(jsonPath("$.data.artifactTypes", Matchers.contains("ASSUMPTION", "DEPENDENCY")));
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.data.type").value("RICH_TEXT"))
                .andExpect(jsonPath("$.data.artifactTypes", Matchers.hasSize(0)));
    }

    private void lockSectionAs(int documentSectionId, String token) throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/" + documentSectionId + "/lock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Adding items\"}").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.flag").value(true));
    }

    /** The document section's items exactly as the GET serves them, which is what the editor sends back. */
    private JSONArray currentItems(int documentSectionId, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/" + documentSectionId)
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONArray("requirementArtifacts");
    }

    private static JSONObject newItem(String type, String title, String content) throws Exception {
        return new JSONObject().put("type", type).put("title", title).put("content", content).put("priority", "MEDIUM").put("notes", "");
    }

    /** Saves a LIST document section of team 1's document 1 with these items, leaving its content as seeded. */
    private JSONObject putSection(int documentSectionId, int version, JSONArray items, String token) throws Exception {
        JSONObject body = new JSONObject().put("type", "LIST").put("requirementArtifacts", items).put("version", version);
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/" + documentSectionId)
                .contentType(MediaType.APPLICATION_JSON).content(body.toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

}
