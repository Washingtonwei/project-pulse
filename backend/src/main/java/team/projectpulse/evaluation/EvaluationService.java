package team.projectpulse.evaluation;

import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.exception.PeerEvaluationIllegalArgumentException;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.rubric.Rating;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.team.Team;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvaluationService {

    private static final String RUBRIC_MISMATCH_MESSAGE = "This evaluation was scored against a different set of criteria and can no longer be updated.";
    private static final String NOT_PREVIOUS_WEEK_MESSAGE = "You can only submit evaluations for the previous week.";
    private static final String WINDOW_CLOSED_MESSAGE = "The submission window for this peer evaluation has closed, so it can no longer be changed.";

    private final PeerEvaluationRepository evaluationRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final Clock clock; // Clock bean is defined in PeerEvaluationToolApplication.java, this object is for unit testing purposes.


    public EvaluationService(PeerEvaluationRepository evaluationRepository, StudentRepository studentRepository, SectionRepository sectionRepository, Clock clock) {
        this.evaluationRepository = evaluationRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.clock = clock;
    }

    public PeerEvaluation addPeerEvaluation(PeerEvaluation newPeerEvaluation) {
        // Make sure the submission week is one of the course section's active weeks and is still open for submission
        requireOpenSubmissionWindow(newPeerEvaluation.getEvaluator().getSection(), newPeerEvaluation.getWeek(), NOT_PREVIOUS_WEEK_MESSAGE);

        // Make sure the evaluator is on a team at all (BR-team-assignment-required): Student.team is optional,
        // because a student is enrolled in a course section before she is assigned to a team
        Team evaluatorTeam = newPeerEvaluation.getEvaluator().getTeam();
        if (evaluatorTeam == null) {
            throw new PeerEvaluationIllegalArgumentException("You must be assigned to a team before submitting a peer evaluation.");
        }

        // Make sure the evaluator and evaluatee are on the same team. Compare team ids: Team does not override
        // equals, so comparing the entities is identity comparison and only happens to hold within one session.
        Integer evaluatorTeamId = evaluatorTeam.getTeamId();
        Team evaluateeTeam = newPeerEvaluation.getEvaluatee().getTeam();
        if (evaluatorTeamId == null || evaluateeTeam == null || !evaluatorTeamId.equals(evaluateeTeam.getTeamId())) {
            throw new PeerEvaluationIllegalArgumentException("The evaluator and evaluatee must be on the same team.");
        }

        // Make sure this is not a duplicate evaluation
        if (this.evaluationRepository.findByWeekAndEvaluatorIdAndEvaluateeId(newPeerEvaluation.getWeek(), newPeerEvaluation.getEvaluator().getId(), newPeerEvaluation.getEvaluatee().getId()).isPresent()) {
            throw new PeerEvaluationIllegalArgumentException("You have already submitted an evaluation for " + newPeerEvaluation.getEvaluatee().getFirstName() + " in this week.");
        }

        return this.evaluationRepository.save(newPeerEvaluation);
    }

    public PeerEvaluation updatePeerEvaluation(Integer evaluationId, PeerEvaluation update) {
        return this.evaluationRepository.findById(evaluationId)
                .map(oldEvaluation -> {
                    // The week is read off the stored evaluation rather than off the update, because the caller
                    // writes the payload and could otherwise name an open week to reopen a closed evaluation.
                    requireOpenSubmissionWindow(oldEvaluation.getEvaluator().getSection(), oldEvaluation.getWeek(), WINDOW_CLOSED_MESSAGE);
                    rescore(oldEvaluation, update.getRatings());
                    oldEvaluation.setPublicComment(update.getPublicComment());
                    oldEvaluation.setPrivateComment(update.getPrivateComment());
                    return this.evaluationRepository.save(oldEvaluation);
                })
                .orElseThrow(() -> new ObjectNotFoundException("evaluation", evaluationId));
    }

    /**
     * Refuses a submission, or an edit of one, whose week lies outside the course section's open submission window.
     *
     * <p><strong>What the window is.</strong> A peer evaluation covers the previous week and the evaluator has
     * that one week to complete it (BR-evaluation-submission-window), and the week has to be one the course admin
     * marked active for the course section (BR-active-weeks). Both conditions are read against the clock at the
     * moment of the request, so the window closes by itself when the calendar week rolls over.
     *
     * <p><strong>Why an edit runs the same check.</strong> The close of the window is itself the lock that makes
     * an evaluation read-only (BR-evaluation-editable-until-close). There is no separate finalize action and
     * {@code PeerEvaluation} carries no submitted or completed flag, so an evaluation is editable exactly while
     * its own week is still the previous week. Until this check was shared with {@link #updatePeerEvaluation}
     * only the create path checked anything, and a student could rewrite the scores and comments of any past
     * evaluation of hers indefinitely, which is the code half of OI-24.
     *
     * @param outOfWindowMessage what to tell the caller when the week is not the previous week, which reads
     *                           differently for a first submission than for an edit
     */
    private void requireOpenSubmissionWindow(Section section, String week, String outOfWindowMessage) {
        if (!section.getActiveWeeks().contains(week)) {
            throw new PeerEvaluationIllegalArgumentException("The submission week is not in the active weeks for the section.");
        }

        if (!previousWeek().equals(week)) {
            throw new PeerEvaluationIllegalArgumentException(outOfWindowMessage);
        }
    }

    /**
     * The one week a peer evaluation may currently be submitted or edited for, as an ISO-8601 week key such as
     * "2026-W37". The week fields are ISO, so a week starts on Monday and the week-based year is the year the
     * week belongs to, which at a year boundary is not always the calendar year of its days.
     */
    private String previousWeek() {
        LocalDate previousWeekDate = LocalDate.now(this.clock).minusWeeks(1);
        WeekFields weekFields = WeekFields.ISO;
        return String.format("%d-W%02d", previousWeekDate.get(weekFields.weekBasedYear()), previousWeekDate.get(weekFields.weekOfWeekBasedYear()));
    }

    /**
     * Re-scores an evaluation's own rating rows from a submitted set of ratings, matching the two by criterion.
     *
     * <p><strong>Why the evaluation's own rows, rather than the submitted ones.</strong> This used to hand the
     * submitted ratings straight to {@code setRatings}. Those objects carried whatever {@code ratingId} the
     * request body named, and {@code PeerEvaluation} cascades to its ratings, so the save re-parented the named
     * rows onto this evaluation: a student could paste another student's rating ids into an update of her own
     * evaluation, take ownership of those rows, and strip the victim's evaluation of its scores. The converter no
     * longer maps a {@code ratingId}, so the submitted ratings arrive transient and are read here only for their
     * criterion and score. The rows written are the ones already hanging off {@code evaluation}, which the caller
     * has been authorized against by the route rule.
     *
     * <p><strong>Matching by criterion.</strong> An evaluation holds exactly one rating per criterion in its
     * section's rubric, and the converter has checked the same of the submission, so the two line up one for one
     * and no criterion can appear twice in the lookup map below. That map does double duty: each criterion is
     * removed as it is consumed, so anything left over at the end is a criterion the submission scored that the
     * evaluation has no row for. Either direction of mismatch means the rubric changed after this evaluation was
     * submitted, which is rejected rather than guessed at. The alternative, creating rows for the new criteria,
     * would leave the superseded ones behind, since the association has no {@code orphanRemoval}.
     *
     * <p>{@code totalScore} is a stored column, not a derived getter, and mutating the ratings does not run the
     * setter that used to keep it current, so it is recalculated explicitly at the end.
     */
    private void rescore(PeerEvaluation evaluation, List<Rating> submittedRatings) {
        Map<Integer, Double> submittedScoresByCriterionId = submittedRatings.stream()
                .collect(Collectors.toMap(rating -> rating.getCriterion().getCriterionId(), Rating::getActualScore));

        for (Rating rating : evaluation.getRatings()) {
            Double submittedScore = submittedScoresByCriterionId.remove(rating.getCriterion().getCriterionId());
            if (submittedScore == null) { // A criterion this evaluation was scored on that the submission does not cover
                throw new PeerEvaluationIllegalArgumentException(RUBRIC_MISMATCH_MESSAGE);
            }
            rating.setActualScore(submittedScore); // Validates the score against the criterion's max
        }
        if (!submittedScoresByCriterionId.isEmpty()) { // A criterion the submission covers that this evaluation has no row for
            throw new PeerEvaluationIllegalArgumentException(RUBRIC_MISMATCH_MESSAGE);
        }

        evaluation.calculateTotalScore();
    }

    public PeerEvaluationAverage getPeerEvaluationAverage(String week, Student student) {
        // Get all evaluations for the student in the given week
        List<PeerEvaluation> evaluations = this.evaluationRepository.findByWeekAndEvaluateeId(week, student.getId());

        PeerEvaluationAverage peerEvaluationAverage = new PeerEvaluationAverage();

        peerEvaluationAverage.setStudentId(student.getId());
        peerEvaluationAverage.setWeek(week);
        peerEvaluationAverage.setFirstName(student.getFirstName());
        peerEvaluationAverage.setLastName(student.getLastName());
        peerEvaluationAverage.setEmail(student.getEmail());
        peerEvaluationAverage.setTeamName(student.getTeam().getTeamName());

        // 1. Convert the evaluations list to a stream; 2. Map each evaluation object to its total score (resulting in a DoubleStream); 3. Compute the average of the total scores.
        peerEvaluationAverage.setAverageTotalScore(evaluations.stream().mapToDouble(PeerEvaluation::getTotalScore).average().orElse(0.0));

        // 1. Convert the evaluations list to a stream; 2. Map each evaluation object to its public comment; 3. Collect the public comments into a new List<String>.
        peerEvaluationAverage.setPublicComments(evaluations.stream().map(PeerEvaluation::getPublicComment).collect(Collectors.toList()));
        peerEvaluationAverage.setPrivateComments(evaluations.stream().map(PeerEvaluation::getPrivateComment).collect(Collectors.toList()));

        // 1. Convert the evaluations list to a stream.
        Map<Integer, Double> criteriaScores = evaluations.stream()
                // 2. Map each evaluation object to its ratings list; 3. Flatten the ratings lists into a single stream.
                .flatMap(evaluation -> evaluation.getRatings().stream())
                // 4. Group the ratings by criterion ID; 5. Compute the average of the actual scores for each criterion.
                .collect(Collectors.groupingBy(
                        rating -> rating.getCriterion().getCriterionId(), // Group by criterion ID
                        Collectors.averagingDouble(Rating::getActualScore) // Compute the average of the actual scores
                ));

        // Convert the criteria scores map to a list of rating averages
        List<RatingAverage> ratingAverages = criteriaScores.entrySet().stream()
                .map(entry -> new RatingAverage(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // Set the rating averages in the peer evaluation average object
        peerEvaluationAverage.setRatingAverages(ratingAverages);
        return peerEvaluationAverage;
    }

    public WeeklyPeerEvaluationReport generateWeeklyPeerEvaluationReportForSection(Integer sectionId, String week) {
        // Create a new weekly peer evaluation report
        WeeklyPeerEvaluationReport report = new WeeklyPeerEvaluationReport();

        report.setSectionName(this.sectionRepository.findById(sectionId).orElseThrow(() -> new ObjectNotFoundException("section", sectionId)).getSectionName());

        // Get all students in the section
        List<Student> students = this.studentRepository.findBySectionSectionId(sectionId).stream().filter(student -> student.getTeam() != null).collect(Collectors.toList());

        // For each student, compute her average total score, collect all public comments and private comments, and compute rating averages for each criterion.
        students.forEach(student -> {
            PeerEvaluationAverage peerEvaluationAverage = getPeerEvaluationAverage(week, student);
            report.getPeerEvaluationAverages().add(peerEvaluationAverage);
        });

        // Find all students who did not submit evaluations for the given week
        List<PeerEvaluation> allEvaluationsInAWeek = this.evaluationRepository.findByWeek(week);
        List<String> studentsMissingPeerEvaluations = students.stream()
                // Filter out students who have submitted evaluations. In other words, being an evaluator of some peer evaluation.
                .filter(student -> allEvaluationsInAWeek.stream().noneMatch(evaluation -> evaluation.getEvaluator().getId().equals(student.getId())))
                .map(student -> student.getFirstName() + " " + student.getLastName())
                .toList();

        report.setStudentsMissingPeerEvaluations(studentsMissingPeerEvaluations);

        report.setWeek(week);
        return report;
    }

    public PeerEvaluationAverage generateWeeklyPeerEvaluationSummaryForStudent(Integer studentId, String week) {
        Student student = this.studentRepository.findById(studentId).orElseThrow(() -> new ObjectNotFoundException("student", studentId));
        PeerEvaluationAverage peerEvaluationAverage = getPeerEvaluationAverage(week, student);
        peerEvaluationAverage.setPrivateComments(null); // Do not include private comments in the summary.
        return peerEvaluationAverage;
    }

    public List<PeerEvaluationAverage> generateEvaluationSummariesForStudent(Integer studentId, String startWeek, String endWeek) {
        Student student = this.studentRepository.findById(studentId).orElseThrow(() -> new ObjectNotFoundException("student", studentId));

        List<String> weeks = getWeeksBetween(startWeek, endWeek);

        List<PeerEvaluationAverage> peerEvaluationAverages = weeks.stream().map(week -> getPeerEvaluationAverage(week, student)).collect(Collectors.toList());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If the user is a student, do not include private comments in the summaries
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_student"))) {
            peerEvaluationAverages.forEach(peerEvaluationAverage -> peerEvaluationAverage.setPrivateComments(null));
        }

        return peerEvaluationAverages;
    }

    /**
     * Get all weeks between the start week and the end week.
     *
     * @param startWeek e.g., "2023-W31"
     * @param endWeek   e.g., "2023-W35"
     * @return
     */
    private List<String> getWeeksBetween(String startWeek, String endWeek) {
        List<String> weeks = new ArrayList<>();

        // Parse the start and end week date strings to LocalDate
        LocalDate startDate = parseWeekDate(startWeek);
        LocalDate endDate = parseWeekDate(endWeek);

        // Format to convert LocalDate back to week date string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-'W'ww");

        // Iterate over the weeks and collect them into the list
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            weeks.add(current.format(formatter));
            current = current.plusWeeks(1);
        }

        return weeks;
    }

    // Helper method to parse week date strings to LocalDate
    private static LocalDate parseWeekDate(String weekDateString) {
        int year = Integer.parseInt(weekDateString.substring(0, 4));
        int week = Integer.parseInt(weekDateString.substring(6));

        return Year.of(year)
                .atDay(1)
                .with(WeekFields.ISO.weekOfYear(), week)
                .with(DayOfWeek.MONDAY);
    }

    public List<PeerEvaluation> getWeeklyEvaluationsForStudent(Integer studentId, String week) {
        return this.evaluationRepository.findByWeekAndEvaluateeId(week, studentId);
    }

    public List<PeerEvaluation> getPeerEvaluationsByEvaluatorIdAndWeek(Integer evaluatorId, String week) {
        return this.evaluationRepository.findByWeekAndEvaluatorId(week, evaluatorId);
    }

}
