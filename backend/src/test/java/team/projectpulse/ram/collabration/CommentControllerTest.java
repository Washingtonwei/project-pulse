package team.projectpulse.ram.collabration;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles(value = "dev")
public class CommentControllerTest {
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
    void listCommentThreadsForDocument() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/comment-threads")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find comment threads successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    void listCommentThreadsForDocument_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/comment-threads")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createCommentThreadForDocument() throws Exception {
        String json = """
                {
                  "content": "Everyone must contribute to the document writing."
                }
                """;

        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents/1/comment-threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create comment thread successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.comments", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.comments[0].content").value("Everyone must contribute to the document writing."));
    }

    @Test
    void listCommentThreadsForDocumentSection() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1/document-sections/1/comment-threads") // SRS, Introduction Section
                        .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find comment threads successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data[0].comments", Matchers.hasSize(2)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createCommentThreadForDocumentSection() throws Exception {
        String json = """
                {
                  "content": "A new comment on the document section."
                }
                """;

        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents/1/document-sections/2/comment-threads") // SYSTEM_PURPOSE section
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create comment thread successfully"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.comments", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.comments[0].content").value("A new comment on the document section."));
    }

    @Test
    void listCommentThreadsForRequirementArtifact() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/requirement-artifacts/9/comment-threads") // FR1: The system shall allow users to log in using their email and password.
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find comment threads successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data[0].comments", Matchers.hasSize(2)));
    }

    @Test
    void createCommentThreadForRequirementArtifact() throws Exception {
        String json = """
                {
                  "content": "This is the top priority requirement."
                }
                """;

        this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts/12/comment-threads") // BO-1: Improve Student Requirements Quality
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create comment thread successfully"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.comments", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.comments[0].content").value("This is the top priority requirement."));
    }

    @Test
    void getCommentThread() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/comment-threads/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.comments", Matchers.hasSize(2)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void addCommentToACommentThread() throws Exception {
        String addCommentJson = """
                { "content": "Sure - but let's define it in the glossary." }
                """;

        this.mockMvc.perform(post(this.baseUrl + "/teams/1/comment-threads/1/comments") // Doc level comment thread
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addCommentJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add comment successfully"))
                .andExpect(jsonPath("$.data.comments", Matchers.hasSize(3)))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void resolveACommentThread() throws Exception {
        String resolveJson = """
                { "status": "RESOLVED" }
                """;

        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/comment-threads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolveJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Update comment thread successfully"))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    void getComment() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/comment-threads/1/comments/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("Please add a glossary term for 'artifact' and maybe 'traceability'."));
    }

    @Test
    void getComment_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/comment-threads/1/comments/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void updateComment() throws Exception {
        String updateJson = """
                { "content": "Please add glossary terms for 'artifact' and 'traceability'. (Updated)" }
                """;

        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/comment-threads/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Update comment successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("Please add glossary terms for 'artifact' and 'traceability'. (Updated)"));
    }

    @Test
    void updateComment_NotSameTeam() throws Exception {
        String updateJson = """
                { "content": "Please add glossary terms for 'artifact' and 'traceability'. (Updated)" }
                """;

        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/comment-threads/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteComment() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/comment-threads/1/comments/2")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Delete comment successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        this.mockMvc.perform(get(this.baseUrl + "/teams/1/comment-threads/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.comments", Matchers.hasSize(1)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteCommentThread() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/comment-threads/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Delete comment thread successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

}
