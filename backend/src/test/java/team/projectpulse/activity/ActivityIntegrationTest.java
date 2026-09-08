package team.projectpulse.activity;

import tools.jackson.databind.json.JsonMapper;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

@DisplayName("Integration tests for Activity API endpoints")
public class ActivityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is in section 2

    String studentJanaToken; // Jana is in section 3

    Integer studentJohnId; // The SPA reads these two off the login response and sends them as search criteria

    Integer studentJohnTeamId;

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
        this.studentJohnId = json.getJSONObject("data").getJSONObject("userInfo").getInt("id");
        this.studentJohnTeamId = json.getJSONObject("data").getJSONObject("userInfo").getInt("teamId");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.norton@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
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
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

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
    @DisplayName("A student's search is bound to her own team, whatever the criteria ask for (BR-team-scoped-access)")
    void testStudentJohnFindActivitiesByCriteriaWithoutTeamId() throws Exception {
        // No teamId at all: the search must still return only Team1's week-31 activities (4), not the
        // whole course section's (which would also include Team2's and Team3's).
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("week", "2023-W31");
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "category,asc");

        this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(4)));
    }

    @Test
    @DisplayName("A student searching another team's activities gets nothing back")
    void testStudentJohnFindActivitiesByCriteriaForAnotherTeam() throws Exception {
        // Team2 is in John's own course section, and its week-31 activities exist (6 and 7).
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", "2");
        searchCriteria.put("week", "2023-W31");
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "10");
        requestParams.add("sort", "category,asc");

        this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void testStudentJohnFindActivitiesByCriteria() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", "1");
        searchCriteria.put("week", "2023-W31");
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

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
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

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
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

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
    @DisplayName("MyActivities.vue: a student searching her own studentId and week gets her own activities")
    void testStudentJohnFindActivitiesLikeTheMyActivitiesPage() throws Exception {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("studentId", String.valueOf(this.studentJohnId));
        searchCriteria.put("week", "2023-W31");

        searchActivities(searchCriteria, this.studentJohnToken)
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].studentId", Matchers.everyItem(Matchers.is(this.studentJohnId))));
    }

    @Test
    @DisplayName("TeamsActivities.vue: a student searching her own teamId and week gets her whole team")
    void testStudentJohnFindActivitiesLikeTheTeamsActivitiesPage() throws Exception {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("teamId", String.valueOf(this.studentJohnTeamId));
        searchCriteria.put("week", "2023-W31");

        searchActivities(searchCriteria, this.studentJohnToken)
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(4)))
                .andExpect(jsonPath("$.data.content[*].teamId", Matchers.everyItem(Matchers.is(this.studentJohnTeamId))));
    }

    @Test
    @DisplayName("SectionsActivities.vue: an instructor searching a week alone still gets every team in her course section")
    void testAdminBingyangFindActivitiesLikeTheSectionsActivitiesPage() throws Exception {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("week", "2023-W31");

        searchActivities(searchCriteria, this.adminBingyangToken)
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)))
                // The page groups by team, so it needs more than one team back
                .andExpect(jsonPath("$.data.content[*].teamId", Matchers.hasItems(1, 2, 3)));
    }

    @Test
    @DisplayName("StudentActivities.vue: an instructor searching one student over a week range gets that student")
    void testAdminBingyangFindActivitiesLikeTheStudentActivitiesPage() throws Exception {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("studentId", String.valueOf(this.studentJohnId));
        searchCriteria.put("startWeek", "2023-W31");
        searchCriteria.put("endWeek", "2023-W33");

        searchActivities(searchCriteria, this.adminBingyangToken)
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)))
                .andExpect(jsonPath("$.data.content[*].studentId", Matchers.everyItem(Matchers.is(this.studentJohnId))));
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
    @DisplayName("A student cannot read another team's activity, even one in her own course section (BR-team-scoped-access)")
    void testStudentJohnFindActivityById6InAnotherTeam() throws Exception {
        // Activity 6 belongs to Team2; John is on Team1. Both teams are in course section 2.
        this.mockMvc.perform(get(this.baseUrl + "/activities/6").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DisplayName("A student reads a teammate's activity in her own team")
    void testStudentJohnFindActivityById3InOwnTeam() throws Exception {
        // Activity 3 is Eric's; Eric and John are both on Team1.
        this.mockMvc.perform(get(this.baseUrl + "/activities/3").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find activity successfully"))
                .andExpect(jsonPath("$.data.activityId").value(3))
                .andExpect(jsonPath("$.data.category").value("DOCUMENTATION"));
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

        String json = this.jsonMapper.writeValueAsString(activityDto);

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

        String json = this.jsonMapper.writeValueAsString(activityDto);

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

        String json = this.jsonMapper.writeValueAsString(activityDto);

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/activities/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
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
    @DisplayName("A student cannot comment on another team's activity in her own course section")
    void testStudentJohnAddActivityCommentInAnotherTeam() throws Exception {
        Map<String, Object> comment = new HashMap<>();
        comment.put("comment", "Good job! Keep up the good work!");

        String json = this.jsonMapper.writeValueAsString(comment);

        // Activity 6 belongs to Team2; John is on Team1.
        this.mockMvc.perform(patch(this.baseUrl + "/activities/6/comments").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testStudentJohnAddActivityCommentInSameTeam() throws Exception {
        Map<String, Object> comment = new HashMap<>();
        comment.put("comment", "Good job! Keep up the good work!");

        String json = this.jsonMapper.writeValueAsString(comment);

        this.mockMvc.perform(patch(this.baseUrl + "/activities/3/comments").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add comment successfully"));
    }

    @Test
    void testStudentJanaAddActivityCommentInDifferentSection() throws Exception {
        Map<String, Object> comment = new HashMap<>();
        comment.put("comment", "Good job! Keep up the good work!");

        String json = this.jsonMapper.writeValueAsString(comment);

        this.mockMvc.perform(patch(this.baseUrl + "/activities/3/comments").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJanaToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    private ResultActions searchActivities(Map<String, String> searchCriteria, String token) throws Exception {
        String json = this.jsonMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "200");

        return this.mockMvc.perform(post(this.baseUrl + "/activities/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token));
    }

}
