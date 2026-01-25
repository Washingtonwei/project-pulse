package team.projectpulse.evaluation;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import team.projectpulse.evaluation.dto.PeerEvaluationDto;
import team.projectpulse.evaluation.dto.RatingDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for Evaluation API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
public class EvaluationIntegrationTest {

    /**
     * @MockitoSpyBean creates a Mockito spy and registers it as the active Spring bean.
     * Unlike a full MockitoBean, a spy keeps the real bean behavior but allows stubbing or verification of specific methods.
     * With @MockitoSpyBean, Spring wraps the real Clock bean instead of replacing it with a mock. This means:
     * - The real Clock works normally by default (no NPE in CommandLineRunner)
     * - We then selectively stub specific methods only in the tests that need it
     */
    @MockitoSpyBean
    Clock clock;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    String adminBingyangToken;

    String adminTimToken;

    String studentJohnToken;

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
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testStudentJohnAddEvaluation() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 9, 15); // 2023-W33
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W36", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add peer evaluation successfully"))
                .andExpect(jsonPath("$.data.week").value("2023-W36"))
                .andExpect(jsonPath("$.data.evaluatorId").value(4))
                .andExpect(jsonPath("$.data.evaluatorName").value("John Smith"))
                .andExpect(jsonPath("$.data.evaluateeId").value(5))
                .andExpect(jsonPath("$.data.evaluateeName").value("Eric Hudson"))
                .andExpect(jsonPath("$.data.ratings", Matchers.hasSize(6)))
                .andExpect(jsonPath("$.data.ratings[0].criterionId").value(1))
                .andExpect(jsonPath("$.data.ratings[0].actualScore").value(4.0))
                .andExpect(jsonPath("$.data.totalScore").value(41.0))
                .andExpect(jsonPath("$.data.publicComment").value("Good job"))
                .andExpect(jsonPath("$.data.privateComment").value("Keep it up"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    void testStudentJohnAddEvaluationNotInActiveWeeks() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 8, 8); // 2023-W32
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        // Active weeks are "2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40"
        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W19", 3, "John Smith", 6, "Woody Moon", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("The submission week is not in the active weeks for the section."));
    }

    @Test
    void testStudentJohnAddEvaluationNotForPreviousWeek() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 8, 8); // 2023-W32
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W33", 3, "John Smith", 6, "Woody Moon", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("You can only submit evaluations for the previous week."));
    }

    @Test
    void testStudentJohnAddEvaluationEvaluatorEvaluateeNotOnTheSameTeam() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 8, 8);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W31", 4, "John Smith", 7, "Woody Moon", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("The evaluator and evaluatee must be on the same team."));
    }

    @Test
    void testStudentJohnAddEvaluationDuplicatedEvaluation() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 8, 8); // 2023-W33
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W31", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("You have already submitted an evaluation for Eric in this week."));
    }

    @Test
    void testEvaluatorJohnGetsEvaluationsByEvaluatorIdAndWeek() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/evaluators/4/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Get peer evaluations by evaluator id and week successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data[0].week").value("2023-W31"))
                .andExpect(jsonPath("$.data[0].evaluatorId").value(4))
                .andExpect(jsonPath("$.data[0].evaluateeId").value(5))
                .andExpect(jsonPath("$.data[0].ratings", Matchers.hasSize(6)))
                .andExpect(jsonPath("$.data[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data[0].updatedAt").isNotEmpty());
    }

    @Test
    void testEvaluatorJohnGetsEvaluationsByWrongEvaluatorIdAndWeek() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/evaluators/5/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testEvaluatorJohnUpdatesOwnEvaluation() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(1, 1, 4.0),
                new RatingDto(2, 2, 9.0),
                new RatingDto(3, 3, 7.0),
                new RatingDto(4, 4, 10.0),
                new RatingDto(5, 5, 5.0),
                new RatingDto(6, 6, 6.0)
        );
        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(1, "2023-W31", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(put(this.baseUrl + "/evaluations/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update peer evaluation successfully"))
                .andExpect(jsonPath("$.data.week").value("2023-W31"))
                .andExpect(jsonPath("$.data.ratings").value(Matchers.hasSize(6)))
                .andExpect(jsonPath("$.data.ratings[0].actualScore").value(4.0))
                .andExpect(jsonPath("$.data.totalScore").value(41.0))
                .andExpect(jsonPath("$.data.publicComment").value("Good job"))
                .andExpect(jsonPath("$.data.privateComment").value("Keep it up"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    void testEvaluatorJohnUpdatesAnotherEvaluatorsEvaluation() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(1, 1, 4.0),
                new RatingDto(2, 2, 9.0),
                new RatingDto(3, 3, 7.0),
                new RatingDto(4, 4, 10.0),
                new RatingDto(5, 5, 5.0),
                new RatingDto(6, 6, 6.0)
        );
        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(5, "2023-W31", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(put(this.baseUrl + "/evaluations/5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testAdminBingyangGenerateWeeklyPeerEvaluationForSection() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/sections/2/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Generate weekly evaluations for section successfully"))
                .andExpect(jsonPath("$.data.sectionName").value("2023-2024"))
                .andExpect(jsonPath("$.data.week").value("2023-W31"))
                .andExpect(jsonPath("$.data.peerEvaluationAverages", Matchers.hasSize(11)))
                .andExpect(jsonPath("$.data.peerEvaluationAverages[0].studentId").value(4))
                .andExpect(jsonPath("$.data.peerEvaluationAverages[0].firstName").value("John"))
                .andExpect(jsonPath("$.data.peerEvaluationAverages[0].lastName").value("Smith"))
                .andExpect(jsonPath("$.data.peerEvaluationAverages[0].averageTotalScore").value(Matchers.closeTo(51.33, 0.01)))
                .andExpect(jsonPath("$.data.studentsMissingPeerEvaluations", Matchers.hasSize(2)));
    }

    @Test
    void testStudentJohnGenerateWeeklyPeerEvaluationForSection() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/sections/2/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testAdminTimGenerateWeeklyPeerEvaluationForSection() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/sections/2/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testStudentJohnGenerateOwnWeeklyPeerEvaluationSummary() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/4/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Generate weekly evaluation summary for student successfully"))
                .andExpect(jsonPath("$.data.studentId").value(4))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"))
                .andExpect(jsonPath("$.data.averageTotalScore").value(Matchers.closeTo(51.33, 0.01)))
                .andExpect(jsonPath("$.data.privateComments").isEmpty()) // privateComments MUST be empty
                .andExpect(jsonPath("$.data.ratingAverages", Matchers.hasSize(6)))
                .andExpect(jsonPath("$.data.ratingAverages[0].averageScore").value(10));
    }

    @Test
    void testStudentJohnGenerateAnotherStudentsWeeklyPeerEvaluationSummary() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/5/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testAdminBingyangGetDetailedWeeklyPeerEvaluationsOfSingleStudent() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/5/week/2023-W31/details").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Get weekly evaluations for student successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data[0].evaluatorName").value("John Smith"))
                .andExpect(jsonPath("$.data[0].totalScore").value(Matchers.closeTo(47.0, 0.01)))
                .andExpect(jsonPath("$.data[1].evaluatorName").value("Eric Hudson"))
                .andExpect(jsonPath("$.data[1].totalScore").value(Matchers.closeTo(54.0, 0.01)))
                .andExpect(jsonPath("$.data[2].evaluatorName").value("Jerry Moon"))
                .andExpect(jsonPath("$.data[2].totalScore").value(Matchers.closeTo(49, 0.01)));
    }

    @Test
    void testStudentJohnGetDetailedWeeklyPeerEvaluationsOfSingleStudent() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/4/week/2023-W31/details").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testAdminTimGetDetailedWeeklyPeerEvaluationsOfSingleStudent() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/4/week/2023-W31/details").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testAdminBingyangGenerateWeeklyPeerEvaluationSummariesForSingleStudent() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("startWeek", "2023-W31");
        requestParams.add("endWeek", "2023-W35");

        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/4").params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Generate evaluation summaries for student successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(5)))
                .andExpect(jsonPath("$.data[0].week").value("2023-W31"))
                .andExpect(jsonPath("$.data[0].averageTotalScore").value(Matchers.closeTo(51.33, 0.01)))
                .andExpect(jsonPath("$.data[0].privateComments").exists())
                .andExpect(jsonPath("$.data[1].week").value("2023-W32"))
                .andExpect(jsonPath("$.data[1].averageTotalScore").value(Matchers.closeTo(50.33, 0.01)))
                .andExpect(jsonPath("$.data[1].privateComments").exists());
    }

    @Test
    void testStudentJohnGenerateOwnWeeklyPeerEvaluationSummaries() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("startWeek", "2023-W31");
        requestParams.add("endWeek", "2023-W35");

        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/4").params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Generate evaluation summaries for student successfully"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(5)))
                .andExpect(jsonPath("$.data[0].week").value("2023-W31"))
                .andExpect(jsonPath("$.data[0].averageTotalScore").value(Matchers.closeTo(51.33, 0.01)))
                .andExpect(jsonPath("$.data[0].privateComments").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data[1].week").value("2023-W32"))
                .andExpect(jsonPath("$.data[1].averageTotalScore").value(Matchers.closeTo(50.33, 0.01)))
                .andExpect(jsonPath("$.data[1].privateComments").value(Matchers.nullValue()));
    }

    @Test
    void testStudentJohnGenerateAnotherStudentsWeeklyPeerEvaluationSummaries() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("startWeek", "2023-W31");
        requestParams.add("endWeek", "2023-W35");

        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/5").params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testAdminTimGenerateAnotherStudentsWeeklyPeerEvaluationSummaries() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("startWeek", "2023-W31");
        requestParams.add("endWeek", "2023-W35");

        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/5").params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

}
