package team.projectpulse.system;

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

        for (Section section : reminderEligibleSections) {
            boolean isWarDueToday = todayDay.equals(section.getWarWeeklyDueDay());
            boolean isPeerEvaluationDueToday = todayDay.equals(section.getPeerEvaluationWeeklyDueDay());

            if (!isWarDueToday && !isPeerEvaluationDueToday) continue; // Nothing due today for this section

            String warTime = isWarDueToday ? formatDue(section.getWarDueTime(), today) : null;
            String peerTime = isPeerEvaluationDueToday ? formatDue(section.getPeerEvaluationDueTime(), today) : null;

            String sharedBody = buildSharedBody(section.getSectionName(), warTime, peerTime);

            for (Student student : section.getStudents()) {
                String html = "Hello %s,<br><br>%s".formatted(student.getFirstName(), sharedBody);
                this.emailService.sendReminderEmail(student.getEmail(), "ProjectPulse Submission Reminder", html);
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
