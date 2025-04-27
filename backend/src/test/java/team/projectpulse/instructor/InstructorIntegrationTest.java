package team.projectpulse.instructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for Instructor API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class InstructorIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminBingyangToken;

    String instructorBillToken;

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

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("bill", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.instructorBillToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("tim", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void testFindInstructorsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("firstName", "Bill");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "5");
        requestParams.add("sort", "lastName,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/instructors/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find instructors successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }

    @Test
    void testAdminBingyangFindInstructorBillById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/instructors/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find instructor successfully"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("bill"))
                .andExpect(jsonPath("$.data.firstName").value("Bill"))
                .andExpect(jsonPath("$.data.lastName").value("Gates"))
                .andExpect(jsonPath("$.data.email").value("b.gates@abc.edu"));
    }

    @Test
    void testInstructorBillFindOwnInfoById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/instructors/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find instructor successfully"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("bill"))
                .andExpect(jsonPath("$.data.firstName").value("Bill"))
                .andExpect(jsonPath("$.data.lastName").value("Gates"))
                .andExpect(jsonPath("$.data.email").value("b.gates@abc.edu"));
    }

    @Test
    void testInstructorFindAnotherInstructorsInfoById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/instructors/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testAddInstructor() throws Exception {
        Map<String, String> instructorDto = new HashMap<>();
        instructorDto.put("username", "elon");
        instructorDto.put("firstName", "Elon");
        instructorDto.put("lastName", "Musk");
        instructorDto.put("email", "e.musk@abc.edu");
        instructorDto.put("password", "Abc123456");

        String json = this.objectMapper.writeValueAsString(instructorDto);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("courseId", "1");
        requestParams.add("registrationToken", "registrationToken");
        requestParams.add("role", "instructor");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/instructors").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add instructor successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.username").value("elon"))
                .andExpect(jsonPath("$.data.firstName").value("Elon"))
                .andExpect(jsonPath("$.data.lastName").value("Musk"))
                .andExpect(jsonPath("$.data.email").value("e.musk@abc.edu"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testAddInstructorWithInvalidUserInvitation() throws Exception {
        Map<String, String> instructorDto = new HashMap<>();
        instructorDto.put("username", "jensen");
        instructorDto.put("firstName", "Jensen");
        instructorDto.put("lastName", "Huang");
        instructorDto.put("email", "j.huang@abc.edu");
        instructorDto.put("password", "123456");

        String json = this.objectMapper.writeValueAsString(instructorDto);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("courseId", "1");
        requestParams.add("registrationToken", "invalidRegistrationToken");
        requestParams.add("role", "instructor");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/instructors").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("j.huang@abc.edu is not invited to register. Please contact the course admin."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testInstructorBillUpdatesOwnInfo() throws Exception {
        Map<String, Object> instructorDto = new HashMap<>();
        instructorDto.put("username", "bill");
        instructorDto.put("firstName", "Bill (updated)");
        instructorDto.put("lastName", "Gates");
        instructorDto.put("email", "b.gates@abc.edu");
        instructorDto.put("enabled", false); // Instructor can't change their own enabled status.
        instructorDto.put("roles", "admin"); // Instructor can't change their own role.

        String json = this.objectMapper.writeValueAsString(instructorDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/instructors/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update instructor successfully"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("bill"))
                .andExpect(jsonPath("$.data.firstName").value("Bill (updated)"))
                .andExpect(jsonPath("$.data.lastName").value("Gates"))
                .andExpect(jsonPath("$.data.email").value("b.gates@abc.edu"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.enabled").value(true)) // The enabled status should not be changed.
                .andExpect(jsonPath("$.data.roles").value("instructor")); // The role should not be changed.
    }

    @Test
    void testInstructorBillUpdatesAnotherInstructorsInfo() throws Exception {
        Map<String, String> instructorDto = new HashMap<>();
        instructorDto.put("username", "bingyang");
        instructorDto.put("firstName", "Bingyang (updated)");
        instructorDto.put("lastName", "Wei");
        instructorDto.put("email", "b.wei@abc.edu");

        String json = this.objectMapper.writeValueAsString(instructorDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/instructors/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testAdminBingyangSetDefaultSection() throws Exception {
        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/instructors/sections/1/default").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Set default section successfully"));

        // Verify that the default section is set.
        this.mockMvc.perform(get(this.baseUrl + "/instructors/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find instructor successfully"))
                .andExpect(jsonPath("$.data.defaultSectionId").value(1));
    }

    @Test
    void testInstructorBillSetDefaultSectionDoesNotBelongToInstructor() throws Exception {
        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/instructors/sections/3/default").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testAdminBingyangSetDefaultCourse() throws Exception {
        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/instructors/courses/2/default").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Set default course successfully"));

        // Verify that the default course is set.
        this.mockMvc.perform(get(this.baseUrl + "/instructors/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find instructor successfully"))
                .andExpect(jsonPath("$.data.defaultCourseId").value(2));
    }

    @Test
    void testInstructorBillSetDefaultCourseDoesNotBelongToInstructor() throws Exception {
        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/instructors/courses/2/default").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

}