package team.projectpulse.notification;

import jakarta.persistence.EntityManager;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.activity.Activity;
import team.projectpulse.activity.ActivityCategory;
import team.projectpulse.activity.ActivityStatus;
import team.projectpulse.evaluation.PeerEvaluation;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.system.EmailService;
import team.projectpulse.system.StatusCode;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UC-NOT-remind-non-submitters against the seed data. The dev clock is Sunday 2023-08-20, so the previous week is
 * 2023-W32, an active week of course section 2. For that week John and Eric reported activities and the rest of
 * the teamed students did not; Team1 (John, Eric, Jerry) evaluated every teammate and Team2 and Team3 nobody.
 * Bill Gates is assigned to course section 2 but owns no course; Bingyang Wei is assigned to it and owns its course;
 * Tim Cook owns another course and is not assigned to it.
 */
@DisplayName("Integration tests for the reminder API endpoints")
class ReminderIntegrationTest extends AbstractIntegrationTest {

    private static final String WEEK = "2023-W32";
    private static final List<String> OWING_WAR = List.of(
            "j.moon@abc.edu",
            "w.allen@abc.edu", "a.freeman@abc.edu", "c.manning@abc.edu", "a.wagner@abc.edu",
            "m.huber@abc.edu", "r.maxwell@abc.edu", "j.mckinney@abc.edu", "m.vargas@abc.edu");

    @MockitoSpyBean
    Clock clock;
    @MockitoSpyBean
    EmailService emailService; // Real sends to the Mailpit container; the spy records who was sent what

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String instructorBillToken;
    String adminBingyangToken;
    String adminTimToken;
    String studentJohnToken;


    @BeforeEach
    void setUp() throws Exception {
        this.instructorBillToken = login("b.gates@abc.edu");
        this.adminBingyangToken = login("b.wei@abc.edu");
        this.adminTimToken = login("t.cook@abc.edu");
        this.studentJohnToken = login("j.smith@abc.edu");
    }

    // Main success scenario

    @Test
    void testRemindWeeklyActivityReportEmailsOnlyStudentsWithNoActivity() throws Exception {
        // When
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.week").value(WEEK))
                .andExpect(jsonPath("$.data.sent").value(9))
                .andExpect(jsonPath("$.data.failed", hasSize(0)));

        // Then: John and Eric reported, the unteamed students owe nothing
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodies = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(9)).sendReminderEmail(recipients.capture(), anyString(), bodies.capture());
        assertThat(recipients.getAllValues()).containsExactlyInAnyOrderElementsOf(OWING_WAR);
        // Step 6: names the course section, the item, the week, and its due day and time, and nobody else (CO-ferpa)
        assertThat(bodies.getAllValues().get(0))
                .contains("<strong>2023-2024</strong>")
                .contains("Weekly activity report for the week of 08-07-2023 to 08-13-2023, due Monday, 08-14-2023 at 11:59 PM")
                .doesNotContain("Peer evaluation")
                .doesNotContain("Smith");
    }

    @Test
    void testRemindPeerEvaluationEmailsAStudentWhoEvaluatedOnlySomeTeammates() throws Exception {
        // Given: John's evaluation of Jerry is removed, so he evaluated two of his three teammates (himself included)
        Student john = student("j.smith@abc.edu");
        Student jerry = student("j.moon@abc.edu");
        this.entityManager.createQuery("delete from Rating r where r.peerEvaluation.evaluator = :john and r.peerEvaluation.evaluatee = :jerry and r.peerEvaluation.week = :week")
                .setParameter("john", john).setParameter("jerry", jerry).setParameter("week", WEEK).executeUpdate();
        this.entityManager.createQuery("delete from PeerEvaluation e where e.evaluator = :john and e.evaluatee = :jerry and e.week = :week")
                .setParameter("john", john).setParameter("jerry", jerry).setParameter("week", WEEK).executeUpdate();

        // When
        sendReminders(this.instructorBillToken, "PEER_EVALUATION")
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.sent").value(9));

        // Then: John is reminded; Eric and Jerry, who evaluated everyone, are not
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(9)).sendReminderEmail(recipients.capture(), anyString(), anyString());
        assertThat(recipients.getAllValues()).contains("j.smith@abc.edu").doesNotContain("e.hudson@abc.edu", "j.moon@abc.edu");
    }

    @Test
    void testSubmissionStatusListsOwingStudentsGroupedByTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/sections/2/submission-status").param("item", "WEEKLY_ACTIVITY_REPORT")
                        .header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.item").value("WEEKLY_ACTIVITY_REPORT"))
                .andExpect(jsonPath("$.data.week").value(WEEK))
                .andExpect(jsonPath("$.data.weekRange").value("08-07-2023 to 08-13-2023"))
                .andExpect(jsonPath("$.data.weekActive").value(true))
                .andExpect(jsonPath("$.data.dueDay").value("MONDAY"))
                .andExpect(jsonPath("$.data.dueTime").value("23:59"))
                .andExpect(jsonPath("$.data.teams", hasSize(3)))
                .andExpect(jsonPath("$.data.teams[0].teamName").value("Team1"))
                .andExpect(jsonPath("$.data.teams[0].students", hasSize(1)))
                .andExpect(jsonPath("$.data.teams[0].students[0].firstName").value("Jerry"))
                .andExpect(jsonPath("$.data.teams[1].students", hasSize(4)))
                .andExpect(jsonPath("$.data.teams[2].students", hasSize(4)));
    }

    // Step 6: who is emailed is decided at send time

    @Test
    void testAStudentWhoSubmitsAfterTheListIsShownIsNotEmailed() throws Exception {
        // Given: Jerry is listed, then reports an activity before the instructor confirms
        submissionStatus("WEEKLY_ACTIVITY_REPORT").andExpect(jsonPath("$.data.teams[0].students[0].firstName").value("Jerry"));
        reportActivity(student("j.moon@abc.edu"));

        // When
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(jsonPath("$.data.sent").value(8));

        // Then
        verify(this.emailService, never()).sendReminderEmail(org.mockito.ArgumentMatchers.eq("j.moon@abc.edu"), anyString(), anyString());
    }

    @Test
    void testWhenEveryListedStudentSubmitsBeforeConfirmingNoReminderIsSent() throws Exception {
        // Given: everyone listed reports before the instructor confirms
        submissionStatus("WEEKLY_ACTIVITY_REPORT").andExpect(jsonPath("$.data.teams", hasSize(3)));
        OWING_WAR.forEach(email -> reportActivity(student(email)));

        // When
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.sent").value(0))
                .andExpect(jsonPath("$.data.failed", hasSize(0)));

        // Then
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    // 4a. The previous week is not one of the course section's active weeks

    @Test
    void testRemindInAWeekAfterAnInactiveWeekIsRefused() throws Exception {
        // Given: Monday 2023-07-31, whose previous week 2023-W30 is before course section 2's first active week
        stubClock(LocalDate.of(2023, 7, 31));

        // When
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT", "2023-W30")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value(ReminderController.INACTIVE_WEEK_MESSAGE));

        // Then
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSubmissionStatusForAnInactiveWeekStillListsWhoOwes() throws Exception {
        stubClock(LocalDate.of(2023, 7, 31));

        submissionStatus("WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.week").value("2023-W30"))
                .andExpect(jsonPath("$.data.weekActive").value(false))
                .andExpect(jsonPath("$.data.teams", hasSize(3)));
    }

    // 4b. No student owes the item

    @Test
    void testSubmissionStatusIsEmptyWhenEveryoneSubmitted() throws Exception {
        OWING_WAR.forEach(email -> reportActivity(student(email)));

        submissionStatus("WEEKLY_ACTIVITY_REPORT")
                .andExpect(jsonPath("$.data.weekActive").value(true))
                .andExpect(jsonPath("$.data.teams", hasSize(0)));
    }

    @Test
    void testRemindWeeklyActivityReportWhenEveryoneSubmittedSendsNothing() throws Exception {
        // Given
        OWING_WAR.forEach(email -> reportActivity(student(email)));

        // When
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.sent").value(0))
                .andExpect(jsonPath("$.data.failed", hasSize(0)));

        // Then
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testRemindPeerEvaluationWhenEveryoneSubmittedSendsNothing() throws Exception {
        // Given: Team2 and Team3 evaluate every teammate, as Team1 already has
        evaluateEveryTeammate();
        submissionStatus("PEER_EVALUATION").andExpect(jsonPath("$.data.teams", hasSize(0)));

        // When
        sendReminders(this.instructorBillToken, "PEER_EVALUATION")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.sent").value(0))
                .andExpect(jsonPath("$.data.failed", hasSize(0)));

        // Then
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    // Step 6: the week is the one the instructor reviewed

    @Test
    void testASendAfterThePreviousWeekMovedOnIsRefused() throws Exception {
        // Given: the list was shown for 2023-W32 on Sunday; Confirm lands on Monday 2023-08-21, when the previous
        // week is 2023-W33
        stubClock(LocalDate.of(2023, 8, 21));

        // When
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT", WEEK)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value(ReminderController.WEEK_CHANGED_MESSAGE));

        // Then
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    // Authorization

    @Test
    void testAnInstructorNotAssignedToTheCourseSectionIsRefused() throws Exception {
        submissionStatus(this.adminTimToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
        sendReminders(this.adminTimToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testAnInstructorWhoIsNotACourseAdminAndNotAssignedIsRefused() throws Exception {
        // Given: Bill, a plain instructor, is unassigned from course section 2
        Section section = this.entityManager.find(Section.class, 2);
        Instructor bill = this.entityManager.createQuery("select i from Instructor i where i.email = 'b.gates@abc.edu'", Instructor.class).getSingleResult();
        section.removeInstructor(bill);
        this.entityManager.flush();

        submissionStatus(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
        sendReminders(this.instructorBillToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testAStudentIsRefused() throws Exception {
        submissionStatus(this.studentJohnToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
        sendReminders(this.studentJohnToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testTheCourseAdminWhoOwnsTheCourseIsAdmittedWithoutBeingAssigned() throws Exception {
        // Given: Bingyang owns course section 2's course; unassign him from the course section itself
        Section section = this.entityManager.find(Section.class, 2);
        Instructor bingyang = this.entityManager.createQuery("select i from Instructor i where i.email = 'b.wei@abc.edu'", Instructor.class).getSingleResult();
        section.removeInstructor(bingyang);
        this.entityManager.flush();

        submissionStatus(this.adminBingyangToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true));
        sendReminders(this.adminBingyangToken, "WEEKLY_ACTIVITY_REPORT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(9));
    }

    // Invalid input

    @Test
    void testUnknownItemOrMalformedWeekIsABadRequest() throws Exception {
        submissionStatus("ATTENDANCE")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
        this.mockMvc.perform(get(this.baseUrl + "/sections/2/submission-status").param("item", "PEER_EVALUATION").param("week", "2023-32")
                        .header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
        this.mockMvc.perform(get(this.baseUrl + "/sections/2/submission-status")
                        .header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(status().isBadRequest());
        sendReminders(this.instructorBillToken, "ATTENDANCE")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("The request is malformed."))
                .andExpect(jsonPath("$.data").doesNotExist()); // no parser or DTO class names
        sendReminders(this.instructorBillToken, "PEER_EVALUATION", "2023-32")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("A week must look like 2026-W40."));
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/reminders").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
        this.mockMvc.perform(post(this.baseUrl + "/sections/2/reminders").contentType(MediaType.APPLICATION_JSON).content("{\"item\": \"PEER_EVALUATION\"}")
                        .header(HttpHeaders.AUTHORIZATION, this.instructorBillToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    // The old WAR page read only the first 200 activities of the week

    @Test
    void testSubmissionStatusIsRightWithMoreThanTwoHundredActivitiesInTheWeek() throws Exception {
        // Given: 205 more activities for John, then one for Woody
        Student john = student("j.smith@abc.edu");
        for (int i = 0; i < 205; i++) {
            reportActivity(john);
        }
        reportActivity(student("w.allen@abc.edu"));

        // Then: Woody is not listed; Team2 now owes three
        submissionStatus("WEEKLY_ACTIVITY_REPORT")
                .andExpect(jsonPath("$.data.teams[1].students", hasSize(3)))
                .andExpect(jsonPath("$.data.teams[1].students[*].firstName", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("Woody"))));
    }

    private ResultActions submissionStatus(String item) throws Exception {
        return submissionStatus(this.instructorBillToken, item);
    }

    private ResultActions submissionStatus(String token, String item) throws Exception {
        return this.mockMvc.perform(get(this.baseUrl + "/sections/2/submission-status").param("item", item)
                .header(HttpHeaders.AUTHORIZATION, token));
    }

    // Sends for the week the dialog shows under the dev clock
    private ResultActions sendReminders(String token, String item) throws Exception {
        return sendReminders(token, item, WEEK);
    }

    private ResultActions sendReminders(String token, String item, String week) throws Exception {
        return this.mockMvc.perform(post(this.baseUrl + "/sections/2/reminders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"item\": \"" + item + "\", \"week\": \"" + week + "\"}")
                .header(HttpHeaders.AUTHORIZATION, token));
    }

    private void stubClock(LocalDate date) {
        ZoneId zone = ZoneId.of("America/Chicago");
        given(this.clock.instant()).willReturn(date.atTime(12, 0).atZone(zone).toInstant());
        given(this.clock.getZone()).willReturn(zone);
    }

    private Student student(String email) {
        return this.entityManager.createQuery("select s from Student s where s.email = :email", Student.class)
                .setParameter("email", email).getSingleResult();
    }

    // Every teamed, active student of course section 2 evaluates every active teammate, herself included
    private void evaluateEveryTeammate() {
        List<Student> teamed = this.entityManager.createQuery("select s from Student s where s.section.sectionId = 2 and s.team is not null and s.enabled = true", Student.class)
                .getResultList();
        for (Student evaluator : teamed) {
            for (Student evaluatee : teamed) {
                boolean sameTeam = evaluator.getTeam().getTeamId().equals(evaluatee.getTeam().getTeamId());
                Long existing = this.entityManager.createQuery("select count(e) from PeerEvaluation e where e.evaluator = :evaluator and e.evaluatee = :evaluatee and e.week = :week", Long.class)
                        .setParameter("evaluator", evaluator).setParameter("evaluatee", evaluatee).setParameter("week", WEEK).getSingleResult();
                if (sameTeam && existing == 0) {
                    this.entityManager.persist(new PeerEvaluation(WEEK, evaluator, evaluatee, new ArrayList<>(), "", ""));
                }
            }
        }
        this.entityManager.flush();
    }

    private void reportActivity(Student student) {
        this.entityManager.persist(new Activity(student, WEEK, student.getTeam(), ActivityCategory.DEVELOPMENT,
                "Activity", "Description", 1.0, 1.0, ActivityStatus.IN_PROGRESS));
        this.entityManager.flush();
    }

    private String login(String username) throws Exception {
        String content = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(username, "123456")))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + new JSONObject(content).getJSONObject("data").getString("token");
    }

}
