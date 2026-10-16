package team.projectpulse.notification;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import team.projectpulse.activity.ActivityService;
import team.projectpulse.evaluation.EvaluationService;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The one implementation of BR-submission-owed: which students of a course section still owe which item for a week.
 * The scheduled reminder, the on-demand reminder, and both instructor report pages all ask here, so a student listed
 * as not having submitted and a student who is reminded are always the same student.
 *
 * <p>Submissions are read through the {@code activity} and {@code evaluation} service layers, never their
 * repositories (MNT-feature-locality).
 */
@Service
@Transactional
public class SubmissionStatusService {

    // Grouped by team in the dialog and on the report pages, so order by team first
    private static final Comparator<Student> BY_TEAM_THEN_NAME = Comparator
            .comparing((Student student) -> student.getTeam().getTeamName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Student::getLastName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Student::getFirstName, String.CASE_INSENSITIVE_ORDER);

    private final StudentRepository studentRepository;
    private final ActivityService activityService;
    private final EvaluationService evaluationService;


    public SubmissionStatusService(StudentRepository studentRepository, ActivityService activityService, EvaluationService evaluationService) {
        this.studentRepository = studentRepository;
        this.activityService = activityService;
        this.evaluationService = evaluationService;
    }

    /**
     * The course section's students who owe the item for the week, ordered by team, then last and first name.
     */
    public List<Student> findOwing(Integer sectionId, SubmittableItem item, String week) {
        return new ArrayList<>(findOwedItems(sectionId, List.of(item), week).keySet());
    }

    /**
     * Each student of the course section who owes at least one of the items for the week, mapped to the items she
     * owes, in the order the items were given. A student who owes none of them is absent. Ordered by team, then
     * last and first name.
     */
    public Map<Student, List<SubmittableItem>> findOwedItems(Integer sectionId, Collection<SubmittableItem> items, String week) {
        // Only a student who is on a team and whose account is active owes anything (BR-team-assignment-required,
        // BR-student-lifecycle), and only such a student counts as a teammate she must evaluate
        List<Student> eligible = this.studentRepository.findBySectionSectionId(sectionId).stream()
                .filter(student -> student.getTeam() != null && student.isEnabled())
                .sorted(BY_TEAM_THEN_NAME)
                .toList();

        // Read each kind of submission once for the whole course section, not once per student
        Set<Integer> reportingStudentIds = items.contains(SubmittableItem.WEEKLY_ACTIVITY_REPORT)
                ? this.activityService.findReportingStudentIds(sectionId, week)
                : Set.of();
        Map<Integer, Set<Integer>> evaluateeIdsByEvaluator = items.contains(SubmittableItem.PEER_EVALUATION)
                ? this.evaluationService.findEvaluateeIdsByEvaluator(sectionId, week)
                : Map.of();
        Map<Integer, Set<Integer>> teammateIdsByTeam = eligible.stream()
                .collect(Collectors.groupingBy(student -> student.getTeam().getTeamId(),
                        Collectors.mapping(Student::getId, Collectors.toSet())));

        Map<Student, List<SubmittableItem>> owedItems = new LinkedHashMap<>();
        for (Student student : eligible) {
            List<SubmittableItem> owed = new ArrayList<>();
            for (SubmittableItem item : items) {
                boolean submitted = switch (item) {
                    // At least one activity for the week, in any status
                    case WEEKLY_ACTIVITY_REPORT -> reportingStudentIds.contains(student.getId());
                    // Every active member of her current team evaluated, herself included. Evaluations of anyone
                    // else (a former teammate, a deactivated one) neither count toward this nor are required.
                    case PEER_EVALUATION -> evaluateeIdsByEvaluator.getOrDefault(student.getId(), Set.of())
                            .containsAll(teammateIdsByTeam.get(student.getTeam().getTeamId()));
                };
                if (!submitted) {
                    owed.add(item);
                }
            }
            if (!owed.isEmpty()) {
                owedItems.put(student, owed);
            }
        }
        return owedItems;
    }

}
