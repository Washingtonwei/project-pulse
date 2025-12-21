package team.projectpulse.rubric;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import team.projectpulse.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for Rubric API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class RubricIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminBingyangToken;

    String adminTimToken;

    String studentJohnToken;

    String studentJanaToken; // In course 3

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

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.norton@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJanaToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void adminBingyangFindRubricsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("rubricName", "rubric");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "5");
        requestParams.add("sort", "rubricId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/rubrics/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubrics successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }

    @Test
    void adminTimFindRubricsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("rubricName", "rubric");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "5");
        requestParams.add("sort", "rubricId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/rubrics/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubrics successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void adminBingyangFindRubricById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubric successfully"))
                .andExpect(jsonPath("$.data.rubricId").value(1))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"))
                .andExpect(jsonPath("$.data.criteria", Matchers.hasSize(6)));
    }

    @Test
    void adminTimFindRubricById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void studentJohnFindRubricById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubric successfully"))
                .andExpect(jsonPath("$.data.rubricId").value(1))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"))
                .andExpect(jsonPath("$.data.criteria", Matchers.hasSize(6)));
    }

    @Test
    void studentJanaFindRubricById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void addRubric() throws Exception {
        // Given
        Map<String, String> rubricDto = new HashMap<>();
        rubricDto.put("rubricName", "Peer Eval Rubric v2");

        String json = this.objectMapper.writeValueAsString(rubricDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/rubrics").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add rubric successfully"))
                .andExpect(jsonPath("$.data.rubricId").isNotEmpty())
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v2"))
                .andExpect(jsonPath("$.data.courseId").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangUpdateRubric() throws Exception {
        // Given
        Map<String, String> rubricDto = new HashMap<>();
        rubricDto.put("rubricName", "Peer Eval Rubric v1 (update)");

        String json = this.objectMapper.writeValueAsString(rubricDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update rubric successfully"))
                .andExpect(jsonPath("$.data.rubricId").value(1))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1 (update)"))
                .andExpect(jsonPath("$.data.courseId").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminTimUpdateRubric() throws Exception {
        // Given
        Map<String, String> rubricDto = new HashMap<>();
        rubricDto.put("rubricName", "Peer Eval Rubric v1 (update)");

        String json = this.objectMapper.writeValueAsString(rubricDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

//    @Test
//    void deleteRubric() {
//    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void assignCriterionToRubric() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/rubrics/1/criteria/7").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add criterion to rubric successfully"));

        this.mockMvc.perform(get(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubric successfully"))
                .andExpect(jsonPath("$.data.rubricId").value(1))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"))
                .andExpect(jsonPath("$.data.criteria", Matchers.hasSize(7)));
    }

    @Test
    void assignCriterionToRubricNotInSameCourse() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/rubrics/1/criteria/8").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void removeCriterionFromRubric() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/rubrics/1/criteria/6").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Remove criterion from rubric successfully"));

        this.mockMvc.perform(get(this.baseUrl + "/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubric successfully"))
                .andExpect(jsonPath("$.data.rubricId").value(1))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"))
                .andExpect(jsonPath("$.data.criteria", Matchers.hasSize(5)));
    }

    @Test
    void removeCriterionFromRubricNotInSameCourse() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/rubrics/1/criteria/8").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));

    }

}