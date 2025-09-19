package team.projectpulse.system;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;

@Component
public class WeeklyReminderScheduler {

    private final EmailService emailService;
    private final SectionService sectionService;


    public WeeklyReminderScheduler(EmailService emailService, SectionService sectionService) {
        this.emailService = emailService;
        this.sectionService = sectionService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Chicago") // Every day at 8 AM Central Time
    public void sendWeeklyReminders() {
        ZoneId zone = ZoneId.of("America/Chicago");
        LocalDate today = LocalDate.now(zone);
        DayOfWeek todayDay = today.getDayOfWeek();

        // Get current ISO week key like "2025-W38"
        int w = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int y = today.get(WeekFields.ISO.weekBasedYear());
        String currentWeek = String.format("%d-W%02d", y, w);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a z").withZone(zone);

        // DB returns only sections eligible for reminders this week; students preloaded via @EntityGraph
        List<Section> reminderEligibleSections = this.sectionService.findReminderEligibleSectionsForWeek(currentWeek);

        reminderEligibleSections.forEach(section -> {
            boolean isWarDueToday = todayDay.equals(section.getWarWeeklyDueDay());
            boolean isPeerEvaluationDueToday = todayDay.equals(section.getPeerEvaluationWeeklyDueDay());

            if (isWarDueToday || isPeerEvaluationDueToday) {
                for (Student student : section.getStudents()) {
                    String email = student.getEmail();
                    String studentName = student.getFirstName();

                    StringBuilder body = new StringBuilder();
                    body.append("Hello ").append(studentName).append(",<br><br>");
                    body.append("You have the following items due today for section <strong>")
                            .append(section.getSectionName())
                            .append("</strong>:<br><ul>");

                    String warTime = formatDue(section.getWarDueTime(), today, timeFmt);
                    String peerTime = formatDue(section.getPeerEvaluationDueTime(), today, timeFmt);

                    if (isWarDueToday) {
                        body.append("<li>WAR report by ").append(warTime).append("</li>");
                    }

                    if (isPeerEvaluationDueToday) {
                        body.append("<li>Peer evaluation by ").append(peerTime).append("</li>");
                    }

                    body.append("</ul>");

                    emailService.sendReminderEmail(email, "ProjectPulse Submission Reminder", body.toString());
                }
            }
        });
    }

    private static String formatDue(LocalTime t, LocalDate date, DateTimeFormatter fmt) {
        return t == null ? null : fmt.format(t.atDate(date).atZone(fmt.getZone()));
    }

}
