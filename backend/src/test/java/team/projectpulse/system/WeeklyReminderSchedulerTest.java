package team.projectpulse.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeeklyReminderSchedulerTest {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");

    @Mock
    EmailService emailService;
    @Mock
    SectionService sectionService;

    @Test
    void testSendWeeklyRemindersEmailsEveryStudentOnADueDay() {
        // Given
        // Sep 14, 2026 is a Monday in 2026-W38, and weekly activity reports are due in this section on Mondays
        Section section = sectionDueOnMonday("Fall 2026", List.of("s1@abc.edu", "s2@abc.edu", "s3@abc.edu"));
        given(this.sectionService.findReminderEligibleSectionsForWeek("2026-W38")).willReturn(List.of(section));

        // When
        scheduler(LocalDate.of(2026, 9, 14), true).sendWeeklyReminders();

        // Then
        verify(this.emailService, times(3)).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendWeeklyRemindersContinuesAfterOneStudentsEmailFails() {
        // Given
        // FR-NOT-weekly-reminder promises each student in the course section a reminder, so one address the mail
        // server refuses must not cost the students after it theirs. This run used to stop at the first failure.
        Section section = sectionDueOnMonday("Fall 2026", List.of("s1@abc.edu", "s2@abc.edu", "s3@abc.edu"));
        given(this.sectionService.findReminderEligibleSectionsForWeek("2026-W38")).willReturn(List.of(section));

        // The broad stub has to come first, and it is not decoration. Under strict stubs a call that matches no
        // stubbing throws PotentialStubbingProblem, and the scheduler catches RuntimeException, so Mockito's own
        // complaint would be swallowed and this test would pass while proving nothing about s1 and s3. Mockito
        // uses the last matching stub, so the narrow one below still wins for the recipient it names.
        willDoNothing().given(this.emailService).sendReminderEmail(anyString(), anyString(), anyString());
        willThrow(new RuntimeException("Failed to send reminder email"))
                .given(this.emailService)
                .sendReminderEmail(eq("s2@abc.edu"), anyString(), anyString());

        // When
        scheduler(LocalDate.of(2026, 9, 14), true).sendWeeklyReminders();

        // Then
        // The captor records every attempt, the failed one included, so this says all three students were tried in
        // order. Only s2 is stubbed to throw, so s1 and s3 were also delivered, and s3 is the one that matters: it
        // comes after the failure and used to be skipped.
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(3)).sendReminderEmail(recipients.capture(), anyString(), anyString());
        assertThat(recipients.getAllValues()).containsExactly("s1@abc.edu", "s2@abc.edu", "s3@abc.edu");
    }

    @Test
    void testSendWeeklyRemindersSkipsADayWithNothingDue() {
        // Given
        // Sep 15, 2026 is a Tuesday; this section's peer evaluations are due on Wednesdays, so nothing is due
        Section section = sectionDueOnMonday("Fall 2026", List.of("s1@abc.edu"));
        given(this.sectionService.findReminderEligibleSectionsForWeek("2026-W38")).willReturn(List.of(section));

        // When
        scheduler(LocalDate.of(2026, 9, 15), true).sendWeeklyReminders();

        // Then
        verify(this.emailService, times(0)).sendReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendWeeklyRemindersDoesNothingWhenDisabled() {
        // When
        scheduler(LocalDate.of(2026, 9, 14), false).sendWeeklyReminders();

        // Then
        verify(this.sectionService, times(0)).findReminderEligibleSectionsForWeek(anyString());
        verify(this.emailService, times(0)).sendReminderEmail(anyString(), anyString(), anyString());
    }

    private WeeklyReminderScheduler scheduler(LocalDate today, boolean enabled) {
        Clock fixedClock = Clock.fixed(today.atStartOfDay(ZONE).toInstant(), ZONE);
        return new WeeklyReminderScheduler(this.emailService, this.sectionService, fixedClock, enabled);
    }

    private Section sectionDueOnMonday(String sectionName, List<String> studentEmails) {
        Section section = new Section(sectionName, LocalDate.of(2026, 8, 24), LocalDate.of(2026, 12, 11), true,
                DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.WEDNESDAY, LocalTime.of(23, 59));
        section.setActiveWeeks(List.of("2026-W38"));
        studentEmails.forEach(email -> section.addStudent(new Student(email, "First", "Last", email, "123456", true, "student")));
        return section;
    }

}
