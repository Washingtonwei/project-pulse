package team.projectpulse.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;
import team.projectpulse.system.WeekKeys;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * FR-NOT-weekly-reminder: on a course section's weekly due day for an item, email each student who owes it one
 * reminder listing only what she owes.
 *
 * <p>Both items are about the previous week, so a course section is reminded only when the previous week is one of
 * its active weeks. That reminds the last active week's items in the week after it, and nothing in the first active
 * week, when there is nothing yet to submit. Who owes what is the same determination the on-demand reminder uses
 * ({@link SubmissionStatusService}).
 */
@Component
public class WeeklyReminderScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReminderScheduler.class);

    private final SectionService sectionService;
    private final SubmissionStatusService submissionStatusService;
    private final ReminderService reminderService;
    private final Clock clock; // Calendar time: "due today" moves with the dev profile's fixed clock too
    private final boolean enabled; // Toggle for enabling/disabling reminders


    public WeeklyReminderScheduler(SectionService sectionService,
                                   SubmissionStatusService submissionStatusService,
                                   ReminderService reminderService,
                                   Clock clock,
                                   @Value("${app.reminders.enabled:true}") boolean enabled) {
        this.sectionService = sectionService;
        this.submissionStatusService = submissionStatusService;
        this.reminderService = reminderService;
        this.clock = clock;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${app.reminders.cron}", zone = "${app.timezone}")
    public void sendWeeklyReminders() {
        if (!this.enabled) return; // Reminders are disabled

        DayOfWeek today = LocalDate.now(this.clock).getDayOfWeek();
        String week = WeekKeys.previousWeek(this.clock);

        List<Section> reminderEligibleSections = this.sectionService.findReminderEligibleSectionsForWeek(week);

        // Two levels of isolation, because FR-NOT-weekly-reminder promises a reminder to *each* owing student in
        // *each* eligible course section, and this run is unattended: whatever fails has to be logged and stepped
        // over rather than surfaced to a caller. ReminderService isolates each student's send. The catch below
        // covers everything else about a course section (a lazy-load that fails, a row with an unexpected null),
        // which would otherwise skip every course section after it just as silently.
        for (Section section : reminderEligibleSections) {
            // Read outside the try: if the body failed because this Section cannot be read, calling the same
            // getter from the handler throws again, the exception escapes the loop, and every course section after
            // this one is skipped, which is precisely what the catch exists to prevent.
            String sectionName = section.getSectionName();
            try {
                // A course section with no due day configured for an item is never reminded of it
                List<SubmittableItem> itemsDueToday = Arrays.stream(SubmittableItem.values())
                        .filter(item -> today.equals(item.dueDay(section)))
                        .toList();
                if (itemsDueToday.isEmpty()) continue; // Nothing due today for this section

                Map<Student, List<SubmittableItem>> owedItems = this.submissionStatusService.findOwedItems(section.getSectionId(), itemsDueToday, week);
                ReminderService.ReminderResult result = this.reminderService.remind(section, owedItems, week);
                LOGGER.info("Sent {} of {} weekly reminders for section {}", result.sent(), owedItems.size(), sectionName);
            } catch (RuntimeException e) {
                LOGGER.error("Could not send the weekly reminders for section {}", sectionName, e);
            }
        }
    }

}
