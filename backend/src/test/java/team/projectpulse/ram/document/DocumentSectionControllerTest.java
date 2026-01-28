package team.projectpulse.ram.document;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DocumentSectionControllerTest {

    @Autowired
    MockMvc mockMvc;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is on team 1 in section 2

    String studentEricToken; // Eric is on team 1 in section 2

    String studentWoodyToken; // Woody is on team 2 in section 2

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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
                    "guidance": "Describe functional requirements that are not easily expressed as use cases.\\n\\nUse structured formats such as:\\n- \\"The system shall…\\" statements\\n- EARS (Easy Approach to Requirements Syntax)\\n"
                }
                """;

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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
                    "guidance": "Describe functional requirements that are not easily expressed as use cases.\\n\\nUse structured formats such as:\\n- \\"The system shall…\\" statements\\n- EARS (Easy Approach to Requirements Syntax)\\n"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/16").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update document section content successfully"))
                .andExpect(jsonPath("$.data.id").value(16))
                .andExpect(jsonPath("$.data.content").value("Updated section content goes here."))
                .andExpect(jsonPath("$.data.requirementArtifacts", Matchers.hasSize(1)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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

        json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here."
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update document section content successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("Updated section content goes here."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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

        json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here."
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/documents/1/document-sections/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.LOCKED))
                .andExpect(jsonPath("$.message").value("Document section is locked by another user. You cannot update its content."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateDocumentSectionWithoutRequirementsArtifacts_NotLocked() throws Exception {
        String json = """
                {
                    "id": 1,
                    "sectionKey": "INTRODUCTION",
                    "type": "RICH_TEXT",
                    "content": "Updated section content goes here."
                }
                """;

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void unlockDocumentSection_Instructor() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/documents/1/document-sections/1/lock").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Unlock document section successfully"));
    }

}
