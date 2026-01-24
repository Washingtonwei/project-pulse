package team.projectpulse.course;

import tools.jackson.databind.json.JsonMapper;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
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
@DisplayName("Integration tests for Course API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class CourseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    String adminBingyangToken; // Bingyang is the admin instructor of course 1 and 2.

    String instructorBillToken; // Bill is the instructor of course 1.

    String adminTimToken; // Tim is the admin instructor of course 3.

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    @Container
    static final GenericContainer<?> mailpit = new GenericContainer<>(DockerImageName.parse("axllent/mailpit"))
            .withExposedPorts(1025, 8025);


    @DynamicPropertySource
    static void setMailpitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
    }

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("b.wei@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.adminBingyangToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("b.gates@abc.edu", "123456")));
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.instructorBillToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("t.cook@abc.edu", "123456")));
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void adminBingyangFindCoursesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "courseId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/courses/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find courses successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].courseName").value("COSC 40993 Senior Design"))
                .andExpect(jsonPath("$.data.content[1].courseName").value("CITE 30363 Web Tech"));
    }

    @Test
    void instructorBillFindCoursesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "courseId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/courses/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find courses successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseName").value("COSC 40993 Senior Design"));
    }

    @Test
    void adminTimFindCoursesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "courseId,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/courses/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find courses successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseName").value("EE 30323 Capstone"));
    }

    @Test
    void adminBingyangFindCourseById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/courses/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find course successfully"))
                .andExpect(jsonPath("$.data.courseId").value(1))
                .andExpect(jsonPath("$.data.courseName").value("COSC 40993 Senior Design"));
    }

    @Test
    void instructorBillFindCourseById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/courses/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminTimFindCourseById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/courses/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangAddCourse() throws Exception {
        // Given
        Map<String, String> courseDto = new HashMap<>();
        courseDto.put("courseName", "New course");
        courseDto.put("courseDescription", "New course description");
        String json = this.jsonMapper.writeValueAsString(courseDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/courses").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add course successfully"))
                .andExpect(jsonPath("$.data.courseId").value(4))
                .andExpect(jsonPath("$.data.courseName").value("New course"))
                .andExpect(jsonPath("$.data.courseDescription").value("New course description"));
    }

    @Test
    void instructorBillAddCourse() throws Exception {
        // Given
        Map<String, String> courseDto = new HashMap<>();
        courseDto.put("courseName", "New course");
        courseDto.put("courseDescription", "New course description");
        String json = this.jsonMapper.writeValueAsString(courseDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/courses").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangUpdateCourse() throws Exception {
        // Given
        Map<String, String> courseDto = new HashMap<>();
        courseDto.put("courseName", "COSC 40993 Senior Design (Updated)");
        courseDto.put("courseDescription", "Senior design project course for Computer Science, Computer Information Technology, and Data Science majors");
        String json = this.jsonMapper.writeValueAsString(courseDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/courses/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update course successfully"))
                .andExpect(jsonPath("$.data.courseId").value(1))
                .andExpect(jsonPath("$.data.courseName").value("COSC 40993 Senior Design (Updated)"));
    }

    @Test
    void instructorBillUpdateCourse() throws Exception {
        // Given
        Map<String, String> courseDto = new HashMap<>();
        courseDto.put("courseName", "COSC 40993 Senior Design (Updated)");
        courseDto.put("courseDescription", "Senior design project course for Computer Science, Computer Information Technology, and Data Science majors");
        String json = this.jsonMapper.writeValueAsString(courseDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/courses/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminTimUpdateCourse() throws Exception {
        // Given
        Map<String, String> courseDto = new HashMap<>();
        courseDto.put("courseName", "COSC 40993 Senior Design (Updated)");
        courseDto.put("courseDescription", "Senior design project course for Computer Science, Computer Information Technology, and Data Science majors");
        String json = this.jsonMapper.writeValueAsString(courseDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/courses/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

//    @Test
//    void deleteCourse() {
//    }

}