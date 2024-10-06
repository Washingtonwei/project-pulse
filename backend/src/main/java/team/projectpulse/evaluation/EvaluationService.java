package team.projectpulse.evaluation;

import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.exception.PeerEvaluationIllegalArgumentException;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.rubric.Rating;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
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
        String submissionWeek = newPeerEvaluation.getWeek(); // Get the submission week from the new peer evaluation
        LocalDate currentDate = LocalDate.now(clock);
        LocalDate previousWeekDate = currentDate.minusWeeks(1);

        // Use ISO-8601 week fields, where the week starts on Monday
        WeekFields weekFields = WeekFields.ISO;

        // Get the correct week number using ISO week fields
        int weekNumber = previousWeekDate.get(weekFields.weekOfWeekBasedYear());
        int year = previousWeekDate.get(weekFields.weekBasedYear());
        String formattedPreviousWeek = String.format("%d-W%02d", year, weekNumber);

        // Make sure the submission week is in the active weeks
        Section currentSection = newPeerEvaluation.getEvaluator().getSection();
        if (!currentSection.getActiveWeeks().contains(submissionWeek)) {
            throw new PeerEvaluationIllegalArgumentException("The submission week is not in the active weeks for the section.");
        }

        // Make sure the submission week is the previous week
        if (!formattedPreviousWeek.equals(submissionWeek)) {
            throw new PeerEvaluationIllegalArgumentException("You can only submit evaluations for the previous week.");
        }

        // Make sure the evaluator and evaluatee are on the same team
        if (!newPeerEvaluation.getEvaluator().getTeam().equals(newPeerEvaluation.getEvaluatee().getTeam())) {
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
                    oldEvaluation.setRatings(update.getRatings()); // Replace the ratings list. This is fine because each rating object has a ratingId so Spring Data JPA can update the existing ratings. Cascade type is set to ALL in the PeerEvaluation entity.
                    oldEvaluation.setPublicComment(update.getPublicComment());
                    oldEvaluation.setPrivateComment(update.getPrivateComment());
                    return this.evaluationRepository.save(oldEvaluation);
                })
                .orElseThrow(() -> new ObjectNotFoundException("evaluation", evaluationId));
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
