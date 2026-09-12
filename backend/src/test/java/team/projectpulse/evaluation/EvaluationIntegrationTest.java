package team.projectpulse.evaluation;

import jakarta.persistence.EntityManager;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.evaluation.dto.PeerEvaluationDto;
import team.projectpulse.evaluation.dto.RatingDto;
import team.projectpulse.rubric.Rating;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("Integration tests for Evaluation API endpoints")
public class EvaluationIntegrationTest extends AbstractIntegrationTest {

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

    @Autowired
    PeerEvaluationRepository evaluationRepository;

    @Autowired
    EntityManager entityManager;

    String adminBingyangToken;

    String adminTimToken;

    String studentJohnToken;

    String studentTracyToken; // Tracy is in section 2 but is not assigned to any team

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

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("t.nicholson@abc.edu", "123456")));
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentTracyToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test

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
                .andExpect(jsonPath("$.message").value("That week is not one of the course section's active weeks."));
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
    @DisplayName("A student on no team cannot submit a peer evaluation, and is told which condition is unmet")
    void testStudentTracyWithNoTeamCannotAddEvaluation() throws Exception {
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

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W31", 16, "Tracy Nicholson", 4, "John Smith", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentTracyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("You must be assigned to a team before submitting a peer evaluation."));
    }

    @Test
    @DisplayName("An instructor does not author peer evaluations, and no longer sees a missing-student error")
    void testAdminBingyangCannotAddEvaluation() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W31", 1, "Bingyang Wei", 4, "John Smith", ratingDtos, 41.0, "Good job", "Keep it up", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Only a student may submit a peer evaluation."));
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

    void testEvaluatorJohnUpdatesOwnEvaluation() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(1, 1, 4.0),
                new RatingDto(2, 2, 9.0),
                new RatingDto(3, 3, 7.0),
                new RatingDto(4, 4, 10.0),
                new RatingDto(5, 5, 5.0),
                new RatingDto(6, 6, 6.0)
        );

        // Aug 8, 2023 falls in 2023-W32, which makes 2023-W31 the previous week, so evaluation 1 is still editable
        LocalDate fixedDate = LocalDate.of(2023, 8, 8);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

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
    @DisplayName("An evaluation can no longer be changed once its submission window has closed")
    void testEvaluatorJohnUpdatesOwnEvaluationAfterTheSubmissionWindowClosed() throws Exception {
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(1, 1, 4.0),
                new RatingDto(2, 2, 9.0),
                new RatingDto(3, 3, 7.0),
                new RatingDto(4, 4, 10.0),
                new RatingDto(5, 5, 5.0),
                new RatingDto(6, 6, 6.0)
        );

        // Sep 15, 2023 is weeks past evaluation 1's week of 2023-W31. An evaluation stays editable only while its
        // own week is the previous week (BR-evaluation-submission-window, BR-evaluation-editable-until-close);
        // the request is otherwise entirely legitimate, since John owns evaluation 1 and the route rule passes.
        LocalDate fixedDate = LocalDate.of(2023, 9, 15);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(1, "2023-W31", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Rewritten long after the fact", "Rewritten long after the fact", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(put(this.baseUrl + "/evaluations/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("The submission window for this peer evaluation has closed, so it can no longer be changed."));
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
    @DisplayName("A student on no team reads her own summaries and gets empty ones, not an error")
    void testStudentTracyWithNoTeamGeneratesOwnWeeklyPeerEvaluationSummaries() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("startWeek", "2023-W31");
        requestParams.add("endWeek", "2023-W32");

        // Tracy has never been on a team, so no one has evaluated her: every week is an empty summary with no team
        // name. This used to be a 500 on her own My Peer Evaluations page, from dereferencing her absent team.
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/15").params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentTracyToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data[0].teamName").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].averageTotalScore").value(Matchers.closeTo(0.0, 0.01)))
                .andExpect(jsonPath("$.data[0].publicComments", Matchers.empty()))
                .andExpect(jsonPath("$.data[0].ratingAverages", Matchers.empty()));
    }

    @Test
    @DisplayName("An instructor opens the performance dashboard of a student on no team")
    void testAdminBingyangGeneratesWeeklyPeerEvaluationSummariesForStudentTracyWithNoTeam() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("startWeek", "2023-W31");
        requestParams.add("endWeek", "2023-W32");

        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/15").params(requestParams).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data[0].teamName").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].averageTotalScore").value(Matchers.closeTo(0.0, 0.01)));
    }

    @Test
    @DisplayName("The single-week summary of a student on no team is empty too")
    void testStudentTracyWithNoTeamGeneratesOwnWeeklyPeerEvaluationSummary() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/evaluations/students/15/week/2023-W31").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentTracyToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.studentId").value(15))
                .andExpect(jsonPath("$.data.teamName").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.averageTotalScore").value(Matchers.closeTo(0.0, 0.01)));
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

    // ------------------------------------------------------------------------------------------------------
    // An id arriving in the request body must never reach save().
    //
    // PeerEvaluation.ratings is @OneToMany(cascade = ALL) and PeerEvaluation.setRatings() points every rating's
    // back-reference at the owning evaluation, so a payload rating carrying an existing ratingId would be a
    // detached child handed to a cascade. Both converters used to map an id out of the body, and these three
    // probes were written to find out what the runtime did with it on each write path:
    //
    //   create + borrowed rating ids  -> 500, "Detached entity passed to persist: ...Rating". Nothing was
    //                                    written, but only because Hibernate refuses a detached child on a
    //                                    cascaded persist. That is the ORM's rule, not a check this code made.
    //   update + borrowed rating ids  -> 200 SUCCESS, and the six rating rows were re-pointed at the caller's
    //                                    own evaluation. The defect these three were written to find.
    //   create + borrowed evaluation id -> 500 NullPointerException, because the converter skipped the evaluator
    //                                    when the body carried an id and addPeerEvaluation then dereferenced it.
    //
    // Two of the three were therefore incidental rather than designed, which is how a rejection of this shape
    // gets re-armed by an unrelated refactor. Neither converter maps an id now, and EvaluationService.rescore
    // updates the evaluation's own rating rows in place. All three assert the security property rather than a
    // status code, so they hold whether the request is refused or the borrowed id is ignored.
    // ------------------------------------------------------------------------------------------------------

    /**
     * Evaluation 4 is Eric's (evaluator 5). Guards the seed ordering the probes below depend on, and hands back
     * the ids of the six rating rows that belong to it.
     */
    private List<Integer> ericsRatingIds() {
        PeerEvaluation ericsEvaluation = this.evaluationRepository.findById(4).orElseThrow();
        assertThat(ericsEvaluation.getEvaluator().getId()).isEqualTo(5);
        return ericsEvaluation.getRatings().stream().map(Rating::getRatingId).toList();
    }

    /**
     * Reads straight from the database, past the persistence context this test shares with the request, and
     * reports which evaluation each of the given rating rows now belongs to.
     */
    private List<Integer> owningEvaluationIds(List<Integer> ratingIds) {
        this.entityManager.flush();
        this.entityManager.clear();
        List<?> rows = this.entityManager
                .createNativeQuery("SELECT peer_evaluation_peer_evaluation_id FROM rating WHERE rating_id IN (:ids)")
                .setParameter("ids", ratingIds)
                .getResultList();
        return rows.stream().map(row -> row == null ? null : ((Number) row).intValue()).toList();
    }

    @Test
    @DisplayName("A create carrying another evaluator's rating ids must not adopt those rating rows")
    void testStudentJohnCannotAdoptAnotherEvaluatorsRatingsThroughCreate() throws Exception {
        List<Integer> ericsRatingIds = ericsRatingIds();

        // A genuine create (no evaluationId), so the evaluator is stamped and the request reaches save(). Only the
        // rating ids are borrowed: six of Eric's, one per criterion, so the all-criteria-rated check still passes.
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(ericsRatingIds.get(0), 1, 4.0),
                new RatingDto(ericsRatingIds.get(1), 2, 9.0),
                new RatingDto(ericsRatingIds.get(2), 3, 7.0),
                new RatingDto(ericsRatingIds.get(3), 4, 10.0),
                new RatingDto(ericsRatingIds.get(4), 5, 5.0),
                new RatingDto(ericsRatingIds.get(5), 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 9, 15); // so that the submission week 2023-W36 is the previous week
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(null, "2023-W36", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Borrowed rating ids", "Borrowed rating ids", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject responseJson = new JSONObject(mvcResult.getResponse().getContentAsString());

        // Whether the request is rejected or the borrowed ids are ignored, Eric's rating rows must still belong to
        // Eric's evaluation. Re-pointing them at the new evaluation would strip Eric's evaluation of its ratings.
        // A rejected request wrote nothing, and reading the database back through a transaction the failure has
        // marked rollback-only would throw, so the read only runs when the request succeeded.
        //
        // The create now succeeds with six freshly minted rating ids, so this read is the assertion that matters.
        // Before the converter stopped mapping ratingId it was refused, but by Hibernate rather than by us.
        if (responseJson.getBoolean("flag")) {
            assertThat(owningEvaluationIds(ericsRatingIds)).containsOnly(4);
        }
    }

    @Test
    @DisplayName("An update to one's own evaluation must not adopt another evaluator's rating rows")
    void testStudentJohnCannotAdoptAnotherEvaluatorsRatingsThroughUpdate() throws Exception {
        List<Integer> ericsRatingIds = ericsRatingIds();

        // Evaluation 1 is John's own, so the route rule is satisfied and this is an authorized request. That is
        // the whole point: EvaluationService.updatePeerEvaluation used to replace the ratings list wholesale with
        // whatever the body carried, relying on each rating's id to update the row in place, so authorization on
        // the evaluation said nothing about the rows the request actually reached.
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(ericsRatingIds.get(0), 1, 4.0),
                new RatingDto(ericsRatingIds.get(1), 2, 9.0),
                new RatingDto(ericsRatingIds.get(2), 3, 7.0),
                new RatingDto(ericsRatingIds.get(3), 4, 10.0),
                new RatingDto(ericsRatingIds.get(4), 5, 5.0),
                new RatingDto(ericsRatingIds.get(5), 6, 6.0)
        );

        // The clock is moved into 2023-W32 so that evaluation 1's submission window is open. Without this the
        // window guard refuses the update before it reaches the ratings, and the probe would pass while proving
        // nothing about rating ownership.
        LocalDate fixedDate = LocalDate.of(2023, 8, 8);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        // This is the request that was exploitable: it returned 200 with rating rows 19-24 owned by evaluation 1,
        // so Eric's evaluation lost its scores and John could read them back through his own. Nothing incidental
        // stopped it, because the parent here is a managed entity, which makes the cascade a merge rather than a
        // persist, and Hibernate re-parents a detached child without complaint.
        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(1, "2023-W31", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Borrowed rating ids", "Borrowed rating ids", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        MvcResult mvcResult = this.mockMvc.perform(put(this.baseUrl + "/evaluations/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject responseJson = new JSONObject(mvcResult.getResponse().getContentAsString());

        if (responseJson.getBoolean("flag")) {
            assertThat(owningEvaluationIds(ericsRatingIds)).containsOnly(4);
        }
    }

    @Test
    @DisplayName("A create carrying another evaluator's evaluation id must not overwrite that evaluation")
    void testStudentJohnCannotOverwriteAnotherEvaluatorsEvaluationThroughCreate() throws Exception {
        // Evaluation 4 is Eric's. The POST route is only .authenticated(), so nothing on the way in reads this id.
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        LocalDate fixedDate = LocalDate.of(2023, 9, 15);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(4, "2023-W36", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Overwritten", "Overwritten", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/evaluations").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andDo(print()).andReturn();
        JSONObject responseJson = new JSONObject(mvcResult.getResponse().getContentAsString());

        // The caller must not end up owning evaluation 4. A rejection would satisfy that, and so does what the
        // converter does now: ignore the body id and create under a freshly minted one.
        // It used to be a rejection, but an accidental one: the converter skipped setting the evaluator whenever
        // the body carried an evaluationId, so addPeerEvaluation dereferenced null. Had the stamping moved into
        // the service while the converter still mapped the id, the merge would have succeeded instead.
        if (responseJson.getBoolean("flag")) {
            assertThat(responseJson.getJSONObject("data").getInt("evaluationId")).isNotEqualTo(4);

            this.entityManager.flush();
            this.entityManager.clear();
            PeerEvaluation ericsEvaluation = this.evaluationRepository.findById(4).orElseThrow();
            assertThat(ericsEvaluation.getEvaluator().getId()).isEqualTo(5);
            assertThat(ericsEvaluation.getPublicComment()).isNotEqualTo("Overwritten");
        }
    }

    @Test
    @DisplayName("A duplicated criterion is rejected as invalid input rather than crashing the update")
    void testStudentJohnUpdatesOwnEvaluationWithADuplicatedCriterion() throws Exception {
        // Seven ratings covering all six criteria, with criterion 1 rated twice. The converter used to compare
        // criterion *sets* only, so the duplicate collapsed and a payload like this passed the very check whose
        // message promises "each criterion is rated only once". It then reached Collectors.toMap in
        // EvaluationService.rescore and came back as a 500 on a duplicate key. The converter now compares the
        // submitted count against the deduplicated count as well, which is what this test holds.
        List<RatingDto> ratingDtos = List.of(
                new RatingDto(null, 1, 4.0),
                new RatingDto(null, 1, 5.0),
                new RatingDto(null, 2, 9.0),
                new RatingDto(null, 3, 7.0),
                new RatingDto(null, 4, 10.0),
                new RatingDto(null, 5, 5.0),
                new RatingDto(null, 6, 6.0)
        );

        PeerEvaluationDto peerEvaluationDto = new PeerEvaluationDto(1, "2023-W31", 4, "John Smith", 5, "Eric Hudson", ratingDtos, 41.0, "Duplicated criterion", "Duplicated criterion", null, null);
        String json = this.jsonMapper.writeValueAsString(peerEvaluationDto);

        this.mockMvc.perform(put(this.baseUrl + "/evaluations/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("The ratings are not valid. Please make sure all criteria in the rubric are rated and each criterion is rated only once."));
    }

}
