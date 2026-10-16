package team.projectpulse.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.Section;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Chicago");
    private static final String WEEK = "2026-W40"; // 09-28-2026 to 10-04-2026

    @Mock
    EmailService emailService;

    ReminderService reminderService;
    Section section;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(LocalDate.of(2026, 10, 5).atStartOfDay(ZONE).toInstant(), ZONE);
        this.reminderService = new ReminderService(this.emailService, clock);
        this.section = new Section("Fall 2026", LocalDate.of(2026, 8, 24), LocalDate.of(2026, 12, 11), true,
                DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
    }

    @Test
    void testRemindContinuesAfterOneSendFailsAndReportsWhoWasNotReached() {
        // Given (UC-NOT-remind-non-submitters 6a)
        Student s1 = student("s1@abc.edu", "Ann");
        Student s2 = student("s2@abc.edu", "Ben");
        Student s3 = student("s3@abc.edu", "Cat");
        Map<Student, List<SubmittableItem>> owed = new LinkedHashMap<>();
        owed.put(s1, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT));
        owed.put(s2, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT));
        owed.put(s3, List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT));
        // The broad stub first: under strict stubs an unmatched call throws, and remind() would swallow it
        willDoNothing().given(this.emailService).sendReminderEmail(anyString(), anyString(), anyString());
        willThrow(new RuntimeException("Failed to send reminder email"))
                .given(this.emailService).sendReminderEmail(eq("s2@abc.edu"), anyString(), anyString());

        // When
        ReminderService.ReminderResult result = this.reminderService.remind(this.section, owed, WEEK);

        // Then: s3, after the failure, was still tried
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        verify(this.emailService, times(3)).sendReminderEmail(recipients.capture(), eq(ReminderService.SUBJECT), anyString());
        assertThat(recipients.getAllValues()).containsExactly("s1@abc.edu", "s2@abc.edu", "s3@abc.edu");
        assertThat(result.sent()).isEqualTo(2);
        assertThat(result.failed()).containsExactly(s2);
    }

    @Test
    void testBodyNamesTheCourseSectionEachOwedItemItsWeekAndItsDueDayAndTime() {
        // When
        String body = this.reminderService.body(this.section, student("s1@abc.edu", "Ann"),
                List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT, SubmittableItem.PEER_EVALUATION), WEEK);

        // Then: both items for 2026-W40 fall due in the week after
        assertThat(body)
                .startsWith("Hello Ann,")
                .contains("<strong>Fall 2026</strong>")
                .contains("<li>Weekly activity report for the week of 09-28-2026 to 10-04-2026, due Monday, 10-05-2026 at 11:59 PM CDT</li>")
                .contains("<li>Peer evaluation for the week of 09-28-2026 to 10-04-2026, due Tuesday, 10-06-2026 at 11:59 PM CDT</li>");
    }

    @Test
    void testBodyListsOnlyTheItemsGiven() {
        String body = this.reminderService.body(this.section, student("s1@abc.edu", "Ann"), List.of(SubmittableItem.PEER_EVALUATION), WEEK);

        assertThat(body).contains("Peer evaluation").doesNotContain("Weekly activity report");
    }

    @Test
    void testBodyNamesAnItemWithoutADueDayWithoutOne() {
        Section unconfigured = new Section("Fall 2026", LocalDate.of(2026, 8, 24), LocalDate.of(2026, 12, 11), true,
                null, null, null, null);

        String body = this.reminderService.body(unconfigured, student("s1@abc.edu", "Ann"), List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT), WEEK);

        assertThat(body).contains("<li>Weekly activity report for the week of 09-28-2026 to 10-04-2026</li>");
    }

    @Test
    void testBodyEscapesEveryInterpolatedValue() {
        Section section = new Section("<i>Fall</i> & Co", LocalDate.of(2026, 8, 24), LocalDate.of(2026, 12, 11), true,
                DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));

        String body = this.reminderService.body(section, student("s1@abc.edu", "<b>Ann</b>"), List.of(SubmittableItem.PEER_EVALUATION), WEEK);

        assertThat(body)
                .contains("Hello &lt;b&gt;Ann&lt;/b&gt;,")
                .contains("&lt;i&gt;Fall&lt;/i&gt; &amp; Co")
                .doesNotContain("<b>")
                .doesNotContain("<i>");
    }

    private static Student student(String email, String firstName) {
        return new Student(email, firstName, "Last", email, "123456", true, "student");
    }

}
