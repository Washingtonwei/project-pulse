package team.projectpulse.rubric;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the rubric questions the route rules ask: is the caller the admin of the course that owns this rubric
 * ({@code isRubricOwner}); may the caller see it, as an instructor of its course or as a student whose course
 * section uses it ({@code canAccessRubric}); and, before a criterion is attached to it, do the two share a course
 * ({@code isRubricAndCriterionInSameCourse}).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class RubricSecurityService {

    private final RubricRepository rubricRepository;
    private final UserUtils userUtils;
    private final StudentRepository studentRepository;
    private final CriterionRepository criterionRepository;


    public RubricSecurityService(RubricRepository rubricRepository, UserUtils userUtils, StudentRepository studentRepository, CriterionRepository criterionRepository) {
        this.rubricRepository = rubricRepository;
        this.userUtils = userUtils;
        this.studentRepository = studentRepository;
        this.criterionRepository = criterionRepository;
    }

    public boolean isRubricOwner(Integer rubricId) {
        // Find the rubric by the rubricId
        Rubric rubricToBeAccessed = this.rubricRepository.findById(rubricId).orElse(null);
        if (rubricToBeAccessed == null) {
            return false;
        }

        Integer adminId = rubricToBeAccessed.getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    // Check if the user can access the rubric
    public boolean canAccessRubric(Integer rubricId) {
        Rubric rubricToBeAccessed = this.rubricRepository.findById(rubricId).orElse(null);
        if (rubricToBeAccessed == null) {
            return false;
        }
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt).orElse(null);
            // A course section with no rubric assigned yet matches no rubric
            if (student == null || student.getSection() == null || student.getSection().getRubric() == null) {
                return false;
            }
            // Check if the rubricId in the request URI matches the rubricId of the student's section
            return rubricId.equals(student.getSection().getRubric().getRubricId());
        } else if (hasInstructorRole) {
            // Get the list of instructorIds from the rubric object
            List<Integer> instructorIds = rubricToBeAccessed.getCourse().getInstructors().stream()
                    .map(Instructor::getId)
                    .collect(Collectors.toList());
            return instructorIds.contains(userIdFromJwt);
        } else {
            return false;
        }
    }

    // Check if the criterion and rubric are in the same course
    public boolean isRubricAndCriterionInSameCourse(Integer rubricId, Integer criterionId) {
        // Find the rubric by the rubricId
        Rubric rubric = this.rubricRepository.findById(rubricId).orElse(null);
        // Find the criterion by the criterionId
        Criterion criterion = this.criterionRepository.findById(criterionId).orElse(null);
        if (rubric == null || criterion == null) {
            return false;
        }
        return rubric.getCourse().getCourseId().equals(criterion.getCourse().getCourseId());
    }

}
