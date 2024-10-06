package team.projectpulse.activity;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Activity activity = this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("activity", activityId));
        Integer ownerId = activity.getStudent().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return ownerId.equals(userIdFromJwt);
    }

    // Everyone (students and the instructors) can access the activity if they are in the section
    public boolean canAccessActivity(Integer activityId) {
        Activity activityToBeAccessed = this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("activity", activityId));
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt)
                    .orElseThrow(() -> new ObjectNotFoundException("student", userIdFromJwt));
            // Check if the student is enrolled in the section of the activity
            return student.getSection().getSectionId().equals(activityToBeAccessed.getTeam().getSection().getSectionId());
        } else if (hasInstructorRole) {
            Instructor instructor = this.instructorRepository.findById(userIdFromJwt)
                    .orElseThrow(() -> new ObjectNotFoundException("instructor", userIdFromJwt));
            // Check if the instructor is teaching the section of the activity
            List<Integer> sectionIds = instructor.getSections().stream()
                    .map(section -> section.getSectionId())
                    .collect(Collectors.toList());
            return sectionIds.contains(activityToBeAccessed.getTeam().getSection().getSectionId());
        } else {
            return false;
        }
    }

}
