package team.projectpulse.instructor;

import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class InstructorSecurityService {

    private final InstructorRepository instructorRepository;
    private final UserUtils userUtils;


    public InstructorSecurityService(InstructorRepository instructorRepository, UserUtils userUtils) {
        this.instructorRepository = instructorRepository;
        this.userUtils = userUtils;
    }

    public boolean isInstructorSelf(Integer instructorId) {
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        return instructorId.equals(instructorIdFromJwt);
    }

    /**
     * Check if the user is the admin of the course that the instructor is in.
     *
     * @param instructorId the id of the instructor we need to access, this is NOT the current user's ID
     * @return
     */
    public boolean isCurrentUserAdminOfInstructorCourse(Integer instructorId) {
        Integer adminIdFromJwt = this.userUtils.getUserId();
        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        return instructor.getCourses().stream()
                .anyMatch(course -> course.getCourseAdmin().getId().equals(adminIdFromJwt));
    }

}
