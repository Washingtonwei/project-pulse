package team.projectpulse.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;

@Component
public class WeeklyReminderScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyReminderScheduler.class);

    private final EmailService emailService;
    private final SectionService sectionService;
    private final Clock clock; // Injected clock for time zone awareness
    private final ZoneId zoneId; // Application time zone
    private final boolean enabled; // Toggle for enabling/disabling reminders
    private final DateTimeFormatter timeFmt;

    public WeeklyReminderScheduler(EmailService emailService,
                                   SectionService sectionService,
                                   Clock clock,
                                   @Value("${app.reminders.enabled:true}") boolean enabled) {
        this.emailService = emailService;
        this.sectionService = sectionService;
        this.clock = clock;
        this.zoneId = clock.getZone();
        this.enabled = enabled;
        this.timeFmt = DateTimeFormatter.ofPattern("h:mm a z").withZone(this.zoneId);
    }

    @Scheduled(cron = "${app.reminders.cron}", zone = "${app.timezone}")
    public void sendWeeklyReminders() {
        if (!this.enabled) return; // Reminders are disabled

        LocalDate today = LocalDate.now(clock);
        DayOfWeek todayDay = today.getDayOfWeek(); // E.g., MONDAY, TUESDAY, etc.

        // Get current ISO week key like "2025-W38"
        int w = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int y = today.get(WeekFields.ISO.weekBasedYear());
        String currentWeek = String.format("%d-W%02d", y, w);

        // DB returns only sections eligible for reminders this week; students preloaded via @EntityGraph
        List<Section> reminderEligibleSections = this.sectionService.findReminderEligibleSectionsForWeek(currentWeek);

        // Two levels of isolation, because FR-NOT-weekly-reminder promises a reminder to *each* student in *each*
        // eligible course section, and this run is unattended: whatever fails has to be logged and stepped over
        // rather than surfaced to a caller. The inner catch covers an address the mail server rejects, which used
        // to abort the whole morning at the first bad one. The outer catch covers everything else about a course
        // section (a lazy-load that fails, a row with an unexpected null), which would otherwise skip every course
        // section after it just as silently.
        for (Section section : reminderEligibleSections) {
            // Read outside the try: if the body failed because this Section cannot be read, calling the same
            // getter from the handler throws again, the exception escapes the loop, and every course section after
            // this one is skipped, which is precisely what the outer catch exists to prevent.
            String sectionName = section.getSectionName();
            try {
                boolean isWarDueToday = todayDay.equals(section.getWarWeeklyDueDay());
                boolean isPeerEvaluationDueToday = todayDay.equals(section.getPeerEvaluationWeeklyDueDay());

                if (!isWarDueToday && !isPeerEvaluationDueToday) continue; // Nothing due today for this section

                String warTime = isWarDueToday ? formatDue(section.getWarDueTime(), today) : null;
                String peerTime = isPeerEvaluationDueToday ? formatDue(section.getPeerEvaluationDueTime(), today) : null;

                String sharedBody = buildSharedBody(section.getSectionName(), warTime, peerTime);

                int sent = 0;
                for (Student student : section.getStudents()) {
                    String html = "Hello %s,<br><br>%s".formatted(student.getFirstName(), sharedBody);
                    try {
                        this.emailService.sendReminderEmail(student.getEmail(), "ProjectPulse Submission Reminder", html);
                        sent++;
                    } catch (RuntimeException e) {
                        LOGGER.error("Could not send the weekly reminder to {} in section {}", student.getEmail(), section.getSectionName(), e);
                    }
                }
                LOGGER.info("Sent {} of {} weekly reminders for section {}", sent, section.getStudents().size(), section.getSectionName());
            } catch (RuntimeException e) {
                LOGGER.error("Could not send the weekly reminders for section {}", sectionName, e);
            }
        }
    }

    private String formatDue(LocalTime t, LocalDate date) {
        if (t == null) return null;
        return t.atDate(date).atZone(this.zoneId).format(this.timeFmt);
    }

    private String buildSharedBody(String sectionName, String warTime, String peerTime) {
        StringBuilder b = new StringBuilder(256);
        b.append("You have the following items due today for section <strong>")
                .append(escape(sectionName))
                .append("</strong>:<br><ul>");

        if (warTime != null) {
            b.append("<li>WAR report by ").append(warTime).append("</li>");
        }
        if (peerTime != null) {
            b.append("<li>Peer evaluation by ").append(peerTime).append("</li>");
        }
        b.append("</ul>");
        return b.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
