package team.projectpulse.system;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class WeeklyReminderScheduler {

    private final EmailService emailService;
    private final SectionService sectionService;


    public WeeklyReminderScheduler(EmailService emailService, SectionService sectionService) {
        this.emailService = emailService;
        this.sectionService = sectionService;
    }
    
    @Scheduled(cron = "0 0 8 * * *") // Every day at 8:00 AM
    public void sendWeeklyReminders() {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDay = today.getDayOfWeek();

        List<Section> activeSections = this.sectionService.findAllActiveSectionsWithStudents();
        activeSections.forEach(section -> {
            boolean isWarDueToday = section.getWarWeeklyDueDay() == todayDay;
            boolean isPeerEvaluationDueToday = section.getPeerEvaluationWeeklyDueDay() == todayDay;

            if (isWarDueToday || isPeerEvaluationDueToday) {
                for (Student student : section.getStudents()) {
                    String email = student.getEmail();
                    String studentName = student.getFirstName();

                    StringBuilder body = new StringBuilder();
                    body.append("Hello ").append(studentName).append(",<br><br>");
                    body.append("You have the following items due today for section <strong>")
                            .append(section.getSectionName())
                            .append("</strong>:<br><ul>");

                    if (isWarDueToday) {
                        body.append("<li>WAR report by ").append(section.getWarDueTime()).append("</li>");
                    }

                    if (isPeerEvaluationDueToday) {
                        body.append("<li>Peer evaluation by ").append(section.getPeerEvaluationDueTime()).append("</li>");
                    }

                    body.append("</ul>");

                    emailService.sendReminderEmail(email, "ProjectPulse Submission Reminder", body.toString());
                }
            }
        });
    }

}
