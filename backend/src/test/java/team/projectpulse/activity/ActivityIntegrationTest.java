package team.projectpulse.activity;

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
@DisplayName("Integration tests for Activity API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
public class ActivityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is in section 2

    String studentJanaToken; // Jana is in section 3

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

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("john", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("jana", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJanaToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void testAdminBingyangFindActivitiesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", "1");
        searchCriteria.put("week", "2023-W31");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "category,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activities successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(4)));
    }

    @Test
    void testStudentJohnFindActivitiesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", "1");
        searchCriteria.put("week", "2023-W31");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "category,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activities successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(4)));
    }

    @Test
    void testadminTimTokenFindActivitiesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", "1");
        searchCriteria.put("week", "2023-W31");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "category,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activities successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void testStudentJanaFindActivitiesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", "1");
        searchCriteria.put("week", "2023-W31");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "category,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJanaToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activities successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void testAdminBingyangFindActivityById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/activities/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activity successfully"))
                .andExpect(jsonPath("$.data.activityId").value(1))
                .andExpect(jsonPath("$.data.category").value("DEVELOPMENT"))
                .andExpect(jsonPath("$.data.activity").value("Develop Login Feature"));
    }

    @Test
    void testAdminTimFindActivityById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/activities/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testStudentJohnFindActivityById5() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/activities/5").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activity successfully"))
                .andExpect(jsonPath("$.data.activityId").value(5))
                .andExpect(jsonPath("$.data.category").value("COMMUNICATION"))
                .andExpect(jsonPath("$.data.activity").value("Weekly Team Meeting"));
    }

    @Test
    void testStudentJohnFindActivityById6() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/activities/6").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activity successfully"))
                .andExpect(jsonPath("$.data.activityId").value(6))
                .andExpect(jsonPath("$.data.category").value("BUGFIX"))
                .andExpect(jsonPath("$.data.activity").value("Fix Navigation Bugs"));
    }

    @Test
    void testStudentJanaFindActivityById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/activities/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJanaToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testStudentJohnAddActivity() throws Exception {
        // Given
        Map<String, Object> activityDto = new HashMap<>();
        activityDto.put("week", "2023-W31");
        activityDto.put("category", "DEVELOPMENT");
        activityDto.put("activity", "Integrate Payment Gateway");
        activityDto.put("description", "Integrate Stripe payment gateway into the application");
        activityDto.put("plannedHours", 8.0);
        activityDto.put("actualHours", 7.5);
        activityDto.put("status", "COMPLETED");

        String json = this.objectMapper.writeValueAsString(activityDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/activities").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add activity successfully"))
                .andExpect(jsonPath("$.data.activityId").isNotEmpty())
                .andExpect(jsonPath("$.data.category").value("DEVELOPMENT"))
                .andExpect(jsonPath("$.data.activity").value("Integrate Payment Gateway"))
                .andExpect(jsonPath("$.data.description").value("Integrate Stripe payment gateway into the application"))
                .andExpect(jsonPath("$.data.plannedHours").value(8.0))
                .andExpect(jsonPath("$.data.actualHours").value(7.5))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testStudentJohnUpdatesOwnActivity() throws Exception {
        // Given
        Map<String, Object> activityDto = new HashMap<>();
        activityDto.put("week", "2023-W31");
        activityDto.put("category", "DEVELOPMENT");
        activityDto.put("activity", "Develop Login Feature (updated)");
        activityDto.put("description", "Implement login functionality for the application");
        activityDto.put("plannedHours", 12.0);
        activityDto.put("actualHours", 10.5);
        activityDto.put("status", "COMPLETED");

        String json = this.objectMapper.writeValueAsString(activityDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/activities/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update activity successfully"))
                .andExpect(jsonPath("$.data.activityId").isNotEmpty())
                .andExpect(jsonPath("$.data.category").value("DEVELOPMENT"))
                .andExpect(jsonPath("$.data.activity").value("Develop Login Feature (updated)"))
                .andExpect(jsonPath("$.data.description").value("Implement login functionality for the application"))
                .andExpect(jsonPath("$.data.plannedHours").value(12.0))
                .andExpect(jsonPath("$.data.actualHours").value(10.5))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void testStudentUpdatesAnotherStudentsActivity() throws Exception {
        // Given
        Map<String, Object> activityDto = new HashMap<>();
        activityDto.put("week", "2023-W31");
        activityDto.put("category", "DEVELOPMENT");
        activityDto.put("activity", "Develop Login Feature (updated)");
        activityDto.put("description", "Implement login functionality for the application");
        activityDto.put("plannedHours", 12.0);
        activityDto.put("actualHours", 10.5);
        activityDto.put("status", "COMPLETED");

        String json = this.objectMapper.writeValueAsString(activityDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/activities/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testStudentJohnDeletesOwnActivity() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/activities/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete activity successfully"));
    }

    @Test
    void testStudentDeletesAnotherStudentsActivity() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/activities/3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testStudentJohnAddActivityCommentInSameSection() throws Exception {
        Map<String, Object> comment = new HashMap<>();
        comment.put("comment", "Good job! Keep up the good work!");

        String json = this.objectMapper.writeValueAsString(comment);

        this.mockMvc.perform(patch(this.baseUrl + "/activities/3/comments").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add comment successfully"));
    }

    @Test
    void testStudentJanaAddActivityCommentInDifferentSection() throws Exception {
        Map<String, Object> comment = new HashMap<>();
        comment.put("comment", "Good job! Keep up the good work!");

        String json = this.objectMapper.writeValueAsString(comment);

        this.mockMvc.perform(patch(this.baseUrl + "/activities/3/comments").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJanaToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

}
