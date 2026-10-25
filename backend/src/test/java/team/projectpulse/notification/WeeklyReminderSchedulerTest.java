package team.projectpulse.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;
import team.projectpulse.system.EmailService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * FR-NOT-weekly-reminder. The real {@link ReminderService} composes and sends each email, so these tests see the
 * emails themselves; who owes what is stubbed, because {@link SubmissionStatusService} has its own tests.
 */
@ExtendWith(MockitoExtension.class)
class WeeklyReminderSchedulerTest {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");
    // Monday 2026-09-21 is in 2026-W39, so the previous week, which both items are about, is 2026-W38
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 21);
    private static final String PREVIOUS_WEEK = "2026-W38";

    @Mock
    EmailService emailService;
    @Mock
    SectionService sectionService;
    @Mock
    SubmissionStatusService submissionStatusService;

    @Test
    void testOnTheWarDueDayOnlyStudentsOwingTheWarAreEmailedAboutTheWarAlone() {
        // Given: weekly activity reports are due on Mondays, peer evaluations on Wednesdays
        Section section = section(1, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        given(this.sectionService.findReminderEligibleSectionsForWeek(PREVIOUS_WEEK)).willReturn(List.of(section));
        Student s1 = student("s1@abc.edu");
        given(this.submissionStatusService.findOwedItems(1, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT), PREVIOUS_WEEK))
                .willReturn(Map.of(s1, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT)));

        // When
        scheduler(MONDAY, true).sendWeeklyReminders();

        // Then
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(1)).sendReminderEmail(eq("s1@abc.edu"), eq(ReminderService.SUBJECT), body.capture());
        assertThat(body.getValue())
                .contains("Weekly activity report for the week of 09-14-2026 to 09-20-2026, due Monday, 09-21-2026")
                .doesNotContain("Peer evaluation");
    }

    @Test
    void testAStudentOwingBothItemsDueTheSameDayGetsOneEmailListingBoth() {
        // Given: both items are due on Mondays
        Section section = section(1, DayOfWeek.MONDAY, DayOfWeek.MONDAY);
        given(this.sectionService.findReminderEligibleSectionsForWeek(PREVIOUS_WEEK)).willReturn(List.of(section));
        Student both = student("both@abc.edu");
        Student one = student("one@abc.edu");
        Map<Student, List<SubmittableItem>> owed = new LinkedHashMap<>();
        owed.put(both, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT, SubmittableItem.PEER_EVALUATION));
        owed.put(one, List.of(SubmittableItem.PEER_EVALUATION));
        given(this.submissionStatusService.findOwedItems(1, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT, SubmittableItem.PEER_EVALUATION), PREVIOUS_WEEK))
                .willReturn(owed);

        // When
        scheduler(MONDAY, true).sendWeeklyReminders();

        // Then
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodies = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(2)).sendReminderEmail(recipients.capture(), anyString(), bodies.capture());
        assertThat(recipients.getAllValues()).containsExactly("both@abc.edu", "one@abc.edu");
        assertThat(bodies.getAllValues().get(0)).contains("Weekly activity report").contains("Peer evaluation");
        assertThat(bodies.getAllValues().get(1)).contains("Peer evaluation").doesNotContain("Weekly activity report");
    }

    @Test
    void testTheLastActiveWeeksPeerEvaluationIsRemindedInTheWeekAfterIt() {
        // Given: 2026-W38 is the course section's last active week; Wednesday 2026-09-23 is in the inactive week after
        Section section = section(1, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        section.setActiveWeeks(List.of("2026-W37", PREVIOUS_WEEK));
        given(this.sectionService.findReminderEligibleSectionsForWeek(PREVIOUS_WEEK)).willReturn(List.of(section));
        given(this.submissionStatusService.findOwedItems(1, List.of(SubmittableItem.PEER_EVALUATION), PREVIOUS_WEEK))
                .willReturn(Map.of(student("s1@abc.edu"), List.of(SubmittableItem.PEER_EVALUATION)));

        // When
        scheduler(LocalDate.of(2026, 9, 23), true).sendWeeklyReminders();

        // Then
        verify(this.emailService, times(1)).sendReminderEmail(eq("s1@abc.edu"), anyString(), anyString());
    }

    @Test
    void testNothingIsRemindedInTheFirstActiveWeek() {
        // Given: 2026-W39 is the course section's first active week, so the previous week, 2026-W38, is not active
        // and the eligibility query, asked for the previous week, returns nothing (the mock's default)

        // When
        scheduler(MONDAY, true).sendWeeklyReminders();

        // Then
        verify(this.sectionService).findReminderEligibleSectionsForWeek(PREVIOUS_WEEK);
        verify(this.submissionStatusService, never()).findOwedItems(anyInt(), anyList(), anyString());
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendWeeklyRemindersContinuesAfterOneStudentsEmailFails() {
        // Given
        // FR-NOT-weekly-reminder promises each owing student a reminder, so one address the mail server refuses
        // must not cost the students after it theirs.
        Section section = section(1, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        given(this.sectionService.findReminderEligibleSectionsForWeek(PREVIOUS_WEEK)).willReturn(List.of(section));
        Map<Student, List<SubmittableItem>> owed = new LinkedHashMap<>();
        List.of("s1@abc.edu", "s2@abc.edu", "s3@abc.edu").forEach(email -> owed.put(student(email), List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT)));
        given(this.submissionStatusService.findOwedItems(1, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT), PREVIOUS_WEEK)).willReturn(owed);

        // The broad stub has to come first, and it is not decoration. Under strict stubs a call that matches no
        // stubbing throws PotentialStubbingProblem, and ReminderService catches RuntimeException, so Mockito's own
        // complaint would be swallowed and this test would pass while proving nothing about s1 and s3. Mockito
        // uses the last matching stub, so the narrow one below still wins for the recipient it names.
        willDoNothing().given(this.emailService).sendReminderEmail(anyString(), anyString(), anyString());
        willThrow(new RuntimeException("Failed to send reminder email"))
                .given(this.emailService)
                .sendReminderEmail(eq("s2@abc.edu"), anyString(), anyString());

        // When
        scheduler(MONDAY, true).sendWeeklyReminders();

        // Then: all three were tried in order, s3 after the failure included
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(3)).sendReminderEmail(recipients.capture(), anyString(), anyString());
        assertThat(recipients.getAllValues()).containsExactly("s1@abc.edu", "s2@abc.edu", "s3@abc.edu");
    }

    @Test
    void testSendWeeklyRemindersContinuesAfterOneSectionFails() {
        // Given
        // Isolation has to be per course section as well as per student. Anything thrown while reading a section
        // used to skip every section after it, and nobody is watching an unattended 8am run to notice.
        Section broken = mock(Section.class);
        willThrow(new RuntimeException("Could not read the course section")).given(broken).getWarWeeklyDueDay();
        Section healthy = section(2, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        given(this.sectionService.findReminderEligibleSectionsForWeek(PREVIOUS_WEEK)).willReturn(List.of(broken, healthy));
        given(this.submissionStatusService.findOwedItems(2, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT), PREVIOUS_WEEK))
                .willReturn(Map.of(student("s1@abc.edu"), List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT)));

        // When
        scheduler(MONDAY, true).sendWeeklyReminders();

        // Then
        verify(this.emailService, times(1)).sendReminderEmail(eq("s1@abc.edu"), anyString(), anyString());
    }

    @Test
    void testSendWeeklyRemindersSkipsADayWithNothingDue() {
        // Given: Tuesday 2026-09-22; this section's items are due on Mondays and Wednesdays
        Section section = section(1, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        given(this.sectionService.findReminderEligibleSectionsForWeek(PREVIOUS_WEEK)).willReturn(List.of(section));

        // When
        scheduler(LocalDate.of(2026, 9, 22), true).sendWeeklyReminders();

        // Then
        verify(this.submissionStatusService, never()).findOwedItems(anyInt(), anyList(), anyString());
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendWeeklyRemindersDoesNothingWhenDisabled() {
        // When
        scheduler(MONDAY, false).sendWeeklyReminders();

        // Then
        verify(this.sectionService, never()).findReminderEligibleSectionsForWeek(anyString());
        verify(this.emailService, never()).sendReminderEmail(anyString(), anyString(), anyString());
    }

    private WeeklyReminderScheduler scheduler(LocalDate today, boolean enabled) {
        Clock fixedClock = Clock.fixed(today.atTime(8, 0).atZone(ZONE).toInstant(), ZONE);
        return new WeeklyReminderScheduler(this.sectionService, this.submissionStatusService,
                new ReminderService(this.emailService, fixedClock), fixedClock, enabled);
    }

    private static Section section(int id, DayOfWeek warDueDay, DayOfWeek peerEvaluationDueDay) {
        Section section = new Section("Fall 2026", LocalDate.of(2026, 8, 24), LocalDate.of(2026, 12, 11), true,
                warDueDay, LocalTime.of(23, 59), peerEvaluationDueDay, LocalTime.of(23, 59));
        section.setSectionId(id);
        section.setActiveWeeks(List.of(PREVIOUS_WEEK));
        return section;
    }

    private static Student student(String email) {
        return new Student(email, "First", "Last", email, "123456", true, "student");
    }

}
