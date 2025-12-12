package team.projectpulse.team;

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
@DisplayName("Integration tests for Team API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class TeamIntegrationTest {

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


    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("b.wei@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.adminBingyangToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("b.gates@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.instructorBillToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.smith@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("t.cook@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void adminBingyangFindTeamsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "teamName,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/teams/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find teams successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    @Test
    void adminTimFindTeamsByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "teamName,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/teams/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find teams successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }

    @Test
    void adminBingyangFindTeamById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find team successfully"))
                .andExpect(jsonPath("$.data.teamId").value(1))
                .andExpect(jsonPath("$.data.teamName").value("Team1"))
                .andExpect(jsonPath("$.data.description").value("Team 1 description"))
                .andExpect(jsonPath("$.data.teamWebsiteUrl").value("https://www.team1.com"));
    }

    @Test
    void instructorBillFindTeamById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find team successfully"))
                .andExpect(jsonPath("$.data.teamId").value(1))
                .andExpect(jsonPath("$.data.teamName").value("Team1"))
                .andExpect(jsonPath("$.data.description").value("Team 1 description"))
                .andExpect(jsonPath("$.data.teamWebsiteUrl").value("https://www.team1.com"));
    }

    @Test
    void studentJohnFindTeamById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find team successfully"))
                .andExpect(jsonPath("$.data.teamId").value(1))
                .andExpect(jsonPath("$.data.teamName").value("Team1"))
                .andExpect(jsonPath("$.data.description").value("Team 1 description"))
                .andExpect(jsonPath("$.data.teamWebsiteUrl").value("https://www.team1.com"));
    }

    @Test
    void studentJohnFindTeamById2() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminTimFindTeamById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangAddTeam() throws Exception {
        // Given
        Map<String, Object> teamDto = new HashMap<>();
        teamDto.put("teamName", "Team4");
        teamDto.put("description", "Team 4 description");
        teamDto.put("teamWebsiteUrl", "https://www.team4.com");
        teamDto.put("sectionId", 2);

        String json = this.objectMapper.writeValueAsString(teamDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/teams").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add team successfully"))
                .andExpect(jsonPath("$.data.teamId").isNotEmpty())
                .andExpect(jsonPath("$.data.teamName").value("Team4"))
                .andExpect(jsonPath("$.data.description").value("Team 4 description"))
                .andExpect(jsonPath("$.data.teamWebsiteUrl").value("https://www.team4.com"))
                .andExpect(jsonPath("$.data.sectionId").value(2));
    }

    @Test
    void instructorBillAddTeam() throws Exception {
        // Given
        Map<String, Object> teamDto = new HashMap<>();
        teamDto.put("teamName", "Team4");
        teamDto.put("description", "Team 4 description");
        teamDto.put("teamWebsiteUrl", "https://www.team4.com");
        teamDto.put("sectionId", 2);

        String json = this.objectMapper.writeValueAsString(teamDto);

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/teams").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void adminBingyangUpdateTeam() throws Exception {
        // Given
        Map<String, Object> teamDto = new HashMap<>();
        teamDto.put("teamName", "Team1 (updated)");
        teamDto.put("description", "Team 1 description");
        teamDto.put("teamWebsiteUrl", "https://www.team1.com");
        teamDto.put("sectionId", 2);

        String json = this.objectMapper.writeValueAsString(teamDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update team successfully"))
                .andExpect(jsonPath("$.data.teamId").isNotEmpty())
                .andExpect(jsonPath("$.data.teamName").value("Team1 (updated)"))
                .andExpect(jsonPath("$.data.description").value("Team 1 description"))
                .andExpect(jsonPath("$.data.teamWebsiteUrl").value("https://www.team1.com"))
                .andExpect(jsonPath("$.data.sectionId").value(2));
    }

    @Test
    void instructorBillUpdateTeam() throws Exception {
        // Given
        Map<String, Object> teamDto = new HashMap<>();
        teamDto.put("teamName", "Team1 (updated)");
        teamDto.put("description", "Team 1 description");
        teamDto.put("teamWebsiteUrl", "https://www.team1.com");
        teamDto.put("sectionId", 2);

        String json = this.objectMapper.writeValueAsString(teamDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void adminTimUpdateTeam() throws Exception {
        // Given
        Map<String, Object> teamDto = new HashMap<>();
        teamDto.put("teamName", "Team1 (updated)");
        teamDto.put("description", "Team 1 description");
        teamDto.put("teamWebsiteUrl", "https://www.team1.com");
        teamDto.put("sectionId", 2);

        String json = this.objectMapper.writeValueAsString(teamDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

//    @Test
//    void deleteTeam() {
//
//    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void assignStudentToTeam() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/students/14").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Assign student to team successfully"));
    }

    @Test
    void assignStudentToTeamNotInSameSection() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/teams/4/students/14").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void removeStudentFromTeam() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/students/4").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Remove student from team successfully"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void assignInstructorToTeam() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/teams/3/instructors/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Assign instructor to team successfully"));
    }

    @Test
    void assignInstructorToTeamNotSameSection() throws Exception {
        this.mockMvc.perform(put(this.baseUrl + "/teams/3/instructors/3").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void transferTeamToAnotherSectionSuccess() throws Exception {
        // Given
        Map<String, Object> transferTeamRequest = new HashMap<>();
        transferTeamRequest.put("sectionId", "1");

        String json = this.objectMapper.writeValueAsString(transferTeamRequest);

        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/section").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Transfer team successfully"))
                .andExpect(jsonPath("$.data.teamId").value(1))
                .andExpect(jsonPath("$.data.teamName").value("Team1"))
                .andExpect(jsonPath("$.data.oldSectionId").value(2))
                .andExpect(jsonPath("$.data.newSectionId").value(1));

        // Verify the team is now in the new section: section 1
        this.mockMvc.perform(get(this.baseUrl + "/teams/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find team successfully"))
                .andExpect(jsonPath("$.data.teamId").value(1))
                .andExpect(jsonPath("$.data.sectionId").value(1));
    }

    @Test
    void transferTeamToAnotherSectionNotInSameCourse() throws Exception {
        // Given
        Map<String, Object> transferTeamRequest = new HashMap<>();
        transferTeamRequest.put("sectionId", "3"); // Section 3 belongs to a different course

        String json = this.objectMapper.writeValueAsString(transferTeamRequest);

        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/section").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Cannot transfer team to a section in a different course"));
    }

    @Test
    void transferTeamToAnotherSectionAsNonCourseAdmin() throws Exception {
        // Given
        Map<String, Object> transferTeamRequest = new HashMap<>();
        transferTeamRequest.put("sectionId", "1"); // Section 3 belongs to a different course

        String json = this.objectMapper.writeValueAsString(transferTeamRequest);

        // Let Bill, who is not a course admin, try to transfer the team
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/section").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

}