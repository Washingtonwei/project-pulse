package team.projectpulse.activity;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.team.Team;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the weekly activity report questions the route rules ask: did the caller submit this report
 * ({@code isActivityOwner}), and may the caller reach it at all ({@code canAccessActivity}): a student only within
 * her own team (BR-team-scoped-access), an instructor within any course section she is assigned to
 * (BR-section-scoped-access).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class ActivitySecurityService {

    private final ActivityRepository activityRepository;
    private final UserUtils userUtils;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;


    public ActivitySecurityService(ActivityRepository activityRepository, UserUtils userUtils, StudentRepository studentRepository, InstructorRepository instructorRepository) {
        this.activityRepository = activityRepository;
        this.userUtils = userUtils;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    public boolean isActivityOwner(Integer activityId) {
        Activity activity = this.activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return false;
        }
        Integer ownerId = activity.getStudent().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return ownerId.equals(userIdFromJwt);
    }

    // A student reaches her own team's weekly activity reports; an instructor reaches her course section's
    public boolean canAccessActivity(Integer activityId) {
        Activity activityToBeAccessed = this.activityRepository.findById(activityId).orElse(null);
        if (activityToBeAccessed == null) {
            return false;
        }
        // Activity.team and Activity.student are @ManyToOne(optional = false): an activity always has both
        Team activityTeam = activityToBeAccessed.getTeam();
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt).orElse(null);
            // Student.team is optional: a student on no team reaches no weekly activity report
            if (student == null || student.getTeam() == null) {
                return false;
            }
            // Check if the student is on the team the activity belongs to (BR-team-scoped-access)
            return student.getTeam().getTeamId().equals(activityTeam.getTeamId());
        } else if (hasInstructorRole) {
            Instructor instructor = this.instructorRepository.findById(userIdFromJwt).orElse(null);
            if (instructor == null) {
                return false;
            }
            // Check if the instructor is teaching the course section of the activity (BR-section-scoped-access)
            Section activitySection = activityTeam.getSection();
            List<Integer> sectionIds = instructor.getSections().stream()
                    .map(section -> section.getSectionId())
                    .collect(Collectors.toList());
            return sectionIds.contains(activitySection.getSectionId());
        } else {
            return false;
        }
    }

}
