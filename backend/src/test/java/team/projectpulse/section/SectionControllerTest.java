package team.projectpulse.section;

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
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for Section API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class SectionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminBingyangToken;

    String instructorBillToken;

    String studentJohnToken;

    String adminTimToken;

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

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("john", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("tim", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void adminBingyangFindSectionsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("sectionName", "2023");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "5");
        requestParams.add("sort", "sectionName,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find sections successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    void instructorBillFindSectionsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find sections successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    void adminTimFindSectionsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "5");
        requestParams.add("sort", "sectionName,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find sections successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }

    @Test
    void adminBingyangFindSectionById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find section successfully"))
                .andExpect(jsonPath("$.data.sectionId").value(2))
                .andExpect(jsonPath("$.data.sectionName").value("2023-2024"))
                .andExpect(jsonPath("$.data.startDate").value("07-31-2023"))
                .andExpect(jsonPath("$.data.endDate").value("10-06-2024"))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"));
    }

    @Test
    void studentJohnFindSectionById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find section successfully"))
                .andExpect(jsonPath("$.data.sectionId").value(2))
                .andExpect(jsonPath("$.data.sectionName").value("2023-2024"))
                .andExpect(jsonPath("$.data.startDate").value("07-31-2023"))
                .andExpect(jsonPath("$.data.endDate").value("10-06-2024"))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"));
    }

    @Test
    void studentJohnFindSectionById1() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void instructorBillFindSectionById1() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void instructorBillFindSectionById2() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find section successfully"))
                .andExpect(jsonPath("$.data.sectionId").value(2))
                .andExpect(jsonPath("$.data.sectionName").value("2023-2024"))
                .andExpect(jsonPath("$.data.startDate").value("07-31-2023"))
                .andExpect(jsonPath("$.data.endDate").value("10-06-2024"))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"));
    }

    @Test
    void adminTimFindSectionById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangAddSection() throws Exception {
        // Given
        Map<String, Object> sectionDto = new HashMap<>();
        sectionDto.put("sectionName", "2024-2025");
        sectionDto.put("startDate", "08-19-2024");
        sectionDto.put("endDate", "04-28-2025");
        sectionDto.put("courseId", 1);

        String json = this.objectMapper.writeValueAsString(sectionDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add section successfully"))
                .andExpect(jsonPath("$.data.sectionId").isNotEmpty())
                .andExpect(jsonPath("$.data.sectionName").value("2024-2025"))
                .andExpect(jsonPath("$.data.startDate").value("08-19-2024"))
                .andExpect(jsonPath("$.data.endDate").value("04-28-2025"))
                .andExpect(jsonPath("$.data.courseId").value("1"));

        Map<String, String> searchCriteria = new HashMap<>();
        json = this.objectMapper.writeValueAsString(searchCriteria);

        this.mockMvc.perform(post(this.baseUrl + "/sections/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find sections successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    @Test
    void instructorBillAddSection() throws Exception {
        // Given
        Map<String, Object> sectionDto = new HashMap<>();
        sectionDto.put("sectionName", "2024-2025");
        sectionDto.put("startDate", "08-19-2024");
        sectionDto.put("endDate", "04-28-2025");
        sectionDto.put("courseId", 1);

        String json = this.objectMapper.writeValueAsString(sectionDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangUpdateSection() throws Exception {
        // Given
        Map<String, Object> sectionDto = new HashMap<>();
        sectionDto.put("sectionName", "2023-2024 (updated)");
        sectionDto.put("startDate", "08-16-2023");
        sectionDto.put("endDate", "04-30-2024");

        String json = this.objectMapper.writeValueAsString(sectionDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/sections/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update section successfully"))
                .andExpect(jsonPath("$.data.sectionId").value(2))
                .andExpect(jsonPath("$.data.sectionName").value("2023-2024 (updated)"))
                .andExpect(jsonPath("$.data.startDate").value("08-16-2023"))
                .andExpect(jsonPath("$.data.endDate").value("04-30-2024"))
                .andExpect(jsonPath("$.data.courseId").value(1));
    }

    @Test
    void adminTimUpdateSection() throws Exception {
        // Given
        Map<String, Object> sectionDto = new HashMap<>();
        sectionDto.put("sectionName", "2023-2024 (updated)");
        sectionDto.put("startDate", "08-16-2023");
        sectionDto.put("endDate", "04-30-2024");

        String json = this.objectMapper.writeValueAsString(sectionDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/sections/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

//    @Test
//    void deleteSection() {
//    }

    @Test
    void adminBingyangSetUpActiveWeeks() throws Exception {
        // Given
        List<String> activeWeeks = List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35");
        String json = this.objectMapper.writeValueAsString(activeWeeks);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/weeks").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Set active weeks successfully"));
    }

    @Test
    void instructorBillSetUpActiveWeeks() throws Exception {
        // Given
        List<String> activeWeeks = List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35");
        String json = this.objectMapper.writeValueAsString(activeWeeks);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/weeks").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminTimSetUpActiveWeeks() throws Exception {
        // Given
        List<String> activeWeeks = List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35");
        String json = this.objectMapper.writeValueAsString(activeWeeks);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/weeks").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminBingyangAssignRubric() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/sections/2/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Assign rubric successfully"));
    }

    @Test
    void adminTimAssignRubric() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/sections/3/rubrics/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangAssignInstructor() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/sections/1/instructors/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Assign instructor successfully"));
    }

    @Test
    void adminTimAssignInstructor() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/sections/3/instructors/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminBingyangRemoveInstructor() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/sections/2/instructors/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Remove instructor successfully"));
    }

    @Test
    void adminTimRemoveInstructor() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/sections/2/instructors/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminBingyangTestSendEmailInvitations() throws Exception {
        // Given
        List<String> emails = List.of("l.santos@abc.edu", "m.sharp@abc.edu");

        // Add params to the request
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("courseId", "1");

        String json = this.objectMapper.writeValueAsString(emails);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/students/email-invitations").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Send email invitation successfully"));
    }

    @Test
    void adminTimTestSendEmailInvitations() throws Exception {
        // Given
        List<String> emails = List.of("l.santos@abc.edu", "m.sharp@abc.edu");

        // Add params to the request
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("courseId", "1");

        String json = this.objectMapper.writeValueAsString(emails);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/students/email-invitations").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

}