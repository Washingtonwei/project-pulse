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
import team.projectpulse.system.StatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class DocumentSectionControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

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
                            "notes": ""
                        },
                        {
                            "id": 10,
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "artifactKey": "FR-2",
                            "title": "Validation",
                            "content": "The system shall validate user inputs to ensure data integrity and prevent errors.",
                            "priority": "CRITICAL",
                            "notes": ""
                        },
                        {
                            "type": "FUNCTIONAL_REQUIREMENT",
                            "title": "Exclusive Locking Mechanism",
                            "content": "When a student begins editing a section, the system shall acquire an exclusive lock for that section for that student.",
                            "priority": "HIGH",
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
                // The save stores the content only: the list keeps its two seeded items, and the new entry sent
                // with them is not created (UC-DOC-edit-document; items go through the artifact endpoints).
                .andExpect(jsonPath("$.data.requirementArtifacts", Matchers.hasSize(2)))
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
                // A list sent with only one entry neither drops the seeded items nor adds the new one.
                .andExpect(jsonPath("$.data.requirementArtifacts", Matchers.hasSize(2)));
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


    // ---- The document section save after requirement artifacts became first-class (docs/design/art.md, Tests) ----
    // The seed has one document, team 1's SRS (document 1). Section 16 is NON_USE_CASE_FUNCTIONAL_REQUIREMENTS, which
    // holds FR-1 and FR-2; section 13 is ASSUMPTIONS_DEPENDENCIES; section 1 is the RICH_TEXT INTRODUCTION.

    @Test
    void sectionSave_ProseOnly_ItemsSentWithItChangeNothing() throws Exception {
        lockSectionAs(16, this.studentJohnToken);
        JSONArray items = currentItems(16, this.studentJohnToken);
        items.getJSONObject(0).put("title", "Retitled through the section save");
        items.remove(1);
        items.put(newItem("FUNCTIONAL_REQUIREMENT", "Exclusive locking", "The system shall lock a document section for one student at a time."));
        int version = fetchSectionVersion(1, 1, 16, this.studentJohnToken);

        JSONObject saved = putSection(16, version, items, this.studentJohnToken);
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        JSONArray savedItems = saved.getJSONObject("data").getJSONArray("requirementArtifacts");
        assertThat(savedItems.length()).isEqualTo(2);
        assertThat(savedItems.getJSONObject(0).getString("title")).isEqualTo("User authentication");
        assertThat(savedItems.getJSONObject(1).getString("artifactKey")).isEqualTo("FR-2");
        assertThat(saved.getJSONObject("data").getInt("version")).as("every accepted save increments the version").isGreaterThan(version);
    }

    @Test
    void findDocumentSectionById_ItemsCarryTheirVersion() throws Exception {
        JSONArray items = currentItems(16, this.studentJohnToken);
        assertThat(items.length()).isEqualTo(2);
        assertThat(items.getJSONObject(0).get("version")).isInstanceOf(Integer.class);
        assertThat(items.getJSONObject(0).getString("homeDocumentType")).isEqualTo("SRS");
    }

    @Test
    void sectionSave_InstructorOfAnotherCourseSection_Refused() throws Exception {
        JSONObject refused = putSection(16, 0, new JSONArray(), this.adminTimToken);
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

    /** Saves a LIST document section of team 1's document 1, sending these items, which the save ignores. */
    private JSONObject putSection(int documentSectionId, int version, JSONArray items, String token) throws Exception {
        JSONObject body = new JSONObject().put("type", "LIST").put("requirementArtifacts", items).put("version", version);
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/" + documentSectionId)
                .contentType(MediaType.APPLICATION_JSON).content(body.toString()).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token)).andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

}
