package team.projectpulse.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.system.EmailService;
import team.projectpulse.system.WeekKeys;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sends one submission reminder to each student, listing only the items she owes, and reports who was reached.
 * Used by both the scheduled and the on-demand reminder; deciding who owes what is {@link SubmissionStatusService}'s
 * job, not this one's.
 *
 * <p><strong>Each send is independent.</strong> One address the mail server refuses must not cost the students after
 * it their reminder, and reminders already sent are not recalled. A student counts as sent only when
 * {@link EmailService#sendReminderEmail} returned, which is when the mail server accepted the message: it is
 * synchronous and throws when the send fails.
 *
 * <p><strong>Every interpolated value is HTML-escaped</strong>, because {@code sendReminderEmail} wraps the body in
 * HTML as it is, and names and course section names are typed by people.
 *
 * <p>Deliberately not {@code @Transactional}: it writes nothing, and holding a database transaction open across
 * several SMTP round trips would pin a connection for no benefit.
 */
@Service
public class ReminderService {

    static final String SUBJECT = "ProjectPulse Submission Reminder";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MM-dd-yyyy");
    private static final DateTimeFormatter DUE_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a z");

    private final EmailService emailService;
    private final ZoneId zoneId;


    public ReminderService(EmailService emailService, Clock clock) {
        this.emailService = emailService;
        this.zoneId = clock.getZone();
    }

    /**
     * Emails each student in {@code owedItems} one reminder naming the course section and each item she owes for
     * the week, with its week range and due day and time.
     *
     * @return how many reminders the mail server accepted, and the students it did not
     */
    public ReminderResult remind(Section section, Map<Student, List<SubmittableItem>> owedItems, String week) {
        int sent = 0;
        List<Student> failed = new ArrayList<>();
        for (Map.Entry<Student, List<SubmittableItem>> entry : owedItems.entrySet()) {
            Student student = entry.getKey();
            try {
                this.emailService.sendReminderEmail(student.getEmail(), SUBJECT, body(section, student, entry.getValue(), week));
                sent++;
            } catch (RuntimeException e) {
                LOGGER.error("Could not send a submission reminder to {} in section {}", student.getEmail(), section.getSectionName(), e);
                failed.add(student);
            }
        }
        return new ReminderResult(sent, failed);
    }

    /**
     * The reminder names only the student's own obligation, never anything about other students (CO-ferpa).
     */
    String body(Section section, Student student, List<SubmittableItem> items, String week) {
        StringBuilder b = new StringBuilder(512);
        b.append("Hello ").append(HtmlUtils.htmlEscape(student.getFirstName())).append(",<br><br>")
                .append("You have not yet submitted the following for course section <strong>")
                .append(HtmlUtils.htmlEscape(section.getSectionName()))
                .append("</strong>:<ul>");
        for (SubmittableItem item : items) {
            b.append("<li>").append(item.label()).append(" for the week of ")
                    .append(WeekKeys.dateRange(week));
            String due = due(section, item, week);
            if (due != null) {
                b.append(", due ").append(due);
            }
            b.append("</li>");
        }
        b.append("</ul>");
        return b.toString();
    }

    /**
     * When the item for {@code week} is due: its weekly due day in the week after, at its due time. Without a due
     * day there is no date to name, so the item is named without one; without a due time, the day alone.
     */
    private String due(Section section, SubmittableItem item, String week) {
        DayOfWeek dueDay = item.dueDay(section);
        LocalTime dueTime = item.dueTime(section);
        if (dueDay == null) {
            return null;
        }
        LocalDate dueDate = WeekKeys.monday(week).plusWeeks(1).with(TemporalAdjusters.nextOrSame(dueDay));
        String day = dueDate.format(DUE_DATE_FORMAT);
        return dueTime == null ? day : day + " at " + dueTime.atDate(dueDate).atZone(this.zoneId).format(DUE_TIME_FORMAT);
    }

    /**
     * @param sent   how many reminders the mail server accepted
     * @param failed the students it did not, in the order they were tried
     */
    public record ReminderResult(int sent, List<Student> failed) {
    }

}
