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
@DisplayName("Integration tests for Criterion API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class CriterionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminBingyangToken;

    String adminTimToken;


    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));
    

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("bingyang", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.adminBingyangToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("tim", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void adminBingyangFindPeerEvaluationCriteriaByCriteriaInCourse1() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "criterionId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/criteria/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find peer evaluation criteria successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));
    }

    @Test
    void adminTimFindPeerEvaluationCriteriaByCriteriaInCourse1() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "criterionId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/criteria/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find peer evaluation criteria successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void adminBingyangFindCriterionById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/criteria/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find peer evaluation criterion successfully"))
                .andExpect(jsonPath("$.data.criterionId").value(1))
                .andExpect(jsonPath("$.data.criterion").value("Quality of work"))
                .andExpect(jsonPath("$.data.description").value("How do you rate the quality of this teammate's work? (1-10)"))
                .andExpect(jsonPath("$.data.maxScore").value(10));
    }

    @Test
    void adminTimFindCriterionById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/criteria/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangAddCriterion() throws Exception {
        // Given
        Map<String, String> criterionDto = new HashMap<>();
        criterionDto.put("criterion", "New Criterion");
        criterionDto.put("description", "Description of the criterion. (1-10)");
        criterionDto.put("maxScore", "10");

        String json = this.objectMapper.writeValueAsString(criterionDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/criteria").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add peer evaluation criterion successfully"))
                .andExpect(jsonPath("$.data.criterionId").isNotEmpty())
                .andExpect(jsonPath("$.data.criterion").value("New Criterion"))
                .andExpect(jsonPath("$.data.description").value("Description of the criterion. (1-10)"))
                .andExpect(jsonPath("$.data.maxScore").value(10))
                .andExpect(jsonPath("$.data.courseId").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangUpdateCriterion() throws Exception {
        // Given
        Map<String, String> criterionDto = new HashMap<>();
        criterionDto.put("criterion", "Quality of work (updated)");
        criterionDto.put("description", "How do you rate the quality of this teammate's work? (1-10)");
        criterionDto.put("maxScore", "10");
        String json = this.objectMapper.writeValueAsString(criterionDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/criteria/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update peer evaluation criterion successfully"))
                .andExpect(jsonPath("$.data.criterionId").value(1))
                .andExpect(jsonPath("$.data.criterion").value("Quality of work (updated)"))
                .andExpect(jsonPath("$.data.description").value("How do you rate the quality of this teammate's work? (1-10)"))
                .andExpect(jsonPath("$.data.maxScore").value(10))
                .andExpect(jsonPath("$.data.courseId").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminTimUpdateCriterion() throws Exception {
        // Given
        Map<String, String> criterionDto = new HashMap<>();
        criterionDto.put("criterion", "Quality of work (updated)");
        criterionDto.put("description", "How do you rate the quality of this teammate's work? (1-10)");
        criterionDto.put("maxScore", "10");
        String json = this.objectMapper.writeValueAsString(criterionDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/criteria/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

//    @Test
//    void deleteCriterion() throws Exception {
//        this.mockMvc.perform(delete(this.baseUrl + "/criteria/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Delete peer evaluation criterion successfully"));
//    }

}