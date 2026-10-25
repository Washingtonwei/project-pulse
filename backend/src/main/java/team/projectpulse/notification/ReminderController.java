package team.projectpulse.notification;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.notification.converter.StudentToOwingStudentDtoConverter;
import team.projectpulse.notification.dto.OwingTeamDto;
import team.projectpulse.notification.dto.ReminderRequestDto;
import team.projectpulse.notification.dto.ReminderResultDto;
import team.projectpulse.notification.dto.SubmissionStatusDto;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionService;
import team.projectpulse.student.Student;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import team.projectpulse.system.WeekKeys;
import team.projectpulse.team.Team;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UC-NOT-remind-non-submitters: who still owes an item, and reminding exactly those students.
 *
 * <p>Both routes are guarded by the course section in the path (an assigned instructor or the owning course admin;
 * see {@code SecurityConfiguration}), and everything below is loaded by that {@code sectionId}, never by anything in
 * the request body.
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/sections/{sectionId}")
public class ReminderController {

    static final String INACTIVE_WEEK_MESSAGE = "Reminders are sent only for an active week.";
    static final String WEEK_CHANGED_MESSAGE = "The week has changed since the list was shown. Review the new list before sending.";

    private final SubmissionStatusService submissionStatusService;
    private final ReminderService reminderService;
    private final SectionService sectionService;
    private final StudentToOwingStudentDtoConverter studentToOwingStudentDtoConverter;
    private final Clock clock;


    public ReminderController(SubmissionStatusService submissionStatusService, ReminderService reminderService, SectionService sectionService, StudentToOwingStudentDtoConverter studentToOwingStudentDtoConverter, Clock clock) {
        this.submissionStatusService = submissionStatusService;
        this.reminderService = reminderService;
        this.sectionService = sectionService;
        this.studentToOwingStudentDtoConverter = studentToOwingStudentDtoConverter;
        this.clock = clock;
    }

    /**
     * The students who owe the item for the week, grouped by team (step 4). The week defaults to the previous week,
     * the one the item is currently due for; the report pages pass another to look back.
     */
    @GetMapping("/submission-status")
    public Result getSubmissionStatus(@PathVariable Integer sectionId,
                                      @RequestParam SubmittableItem item,
                                      @RequestParam(required = false) String week) {
        String statusWeek = week == null ? WeekKeys.previousWeek(this.clock) : WeekKeys.requireValid(week);
        Section section = this.sectionService.findSectionById(sectionId);
        List<Student> owing = this.submissionStatusService.findOwing(sectionId, item, statusWeek);
        SubmissionStatusDto status = new SubmissionStatusDto(
                item,
                statusWeek,
                WeekKeys.dateRange(statusWeek),
                section.getActiveWeeks().contains(statusWeek),
                item.dueDay(section),
                item.dueTime(section),
                groupByTeam(owing));
        return new Result(true, StatusCode.SUCCESS, "Find submission status successfully", status);
    }

    /**
     * Emails each student who owes the item for the previous week (steps 6 and 7). Who owes it is recomputed here,
     * at send time, so a student who submitted after the instructor opened the list is not emailed. The week is not
     * recomputed silently: if it is no longer the week the instructor reviewed, the send is refused.
     */
    @PostMapping("/reminders")
    public Result sendReminders(@PathVariable Integer sectionId, @Valid @RequestBody ReminderRequestDto reminderRequestDto) {
        SubmittableItem item = reminderRequestDto.item();
        String week = WeekKeys.previousWeek(this.clock);
        if (!week.equals(reminderRequestDto.week())) {
            throw new IllegalArgumentException(WEEK_CHANGED_MESSAGE);
        }
        Section section = this.sectionService.findSectionById(sectionId);
        // 4a: the dialog already hides the send button for an inactive week; this guards the route itself
        if (!section.getActiveWeeks().contains(week)) {
            throw new IllegalArgumentException(INACTIVE_WEEK_MESSAGE);
        }

        Map<Student, List<SubmittableItem>> owedItems = this.submissionStatusService.findOwedItems(sectionId, List.of(item), week);
        ReminderService.ReminderResult result = this.reminderService.remind(section, owedItems, week);

        ReminderResultDto resultDto = new ReminderResultDto(item, week, result.sent(),
                result.failed().stream().map(this.studentToOwingStudentDtoConverter::convert).toList());
        return new Result(true, StatusCode.SUCCESS, "Send reminders successfully", resultDto);
    }

    // findOwing orders by team, then name, so grouping in encounter order keeps both orders
    private List<OwingTeamDto> groupByTeam(List<Student> owing) {
        Map<Team, List<Student>> byTeam = owing.stream()
                .collect(Collectors.groupingBy(Student::getTeam, LinkedHashMap::new, Collectors.toList()));
        return byTeam.entrySet().stream()
                .map(entry -> new OwingTeamDto(
                        entry.getKey().getTeamId(),
                        entry.getKey().getTeamName(),
                        entry.getValue().stream().map(this.studentToOwingStudentDtoConverter::convert).toList()))
                .toList();
    }

}
