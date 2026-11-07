package team.projectpulse.student;

import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Answers the questions a route asks about one named student: is that student the caller herself
 * ({@code isStudentSelf}), is the caller an instructor of that student's course section
 * ({@code isCurrentUserInstructorOfStudentSection}), and is the caller the course admin of the course that owns
 * that section ({@code isCurrentUserCourseAdminOfStudent}).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class StudentSecurityService {

    private final StudentRepository studentRepository;
    private final UserUtils userUtils;


    public StudentSecurityService(StudentRepository studentRepository, UserUtils userUtils) {
        this.studentRepository = studentRepository;
        this.userUtils = userUtils;
    }

    public boolean isStudentSelf(Integer studentId) {
        Integer studentIdFromJwt = this.userUtils.getUserId();
        return studentId.equals(studentIdFromJwt);
    }

    /**
     * Check if the user is the instructor of the section that the student is in
     *
     * @param studentId the id of the student we need to access, this is NOT the current user's Id
     * @return
     */
    public boolean isCurrentUserInstructorOfStudentSection(Integer studentId) {
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        Student student = this.studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return false;
        }
        return student.getSection().getInstructors().stream()
                .anyMatch(instructor -> instructor.getId().equals(instructorIdFromJwt));
    }

    /**
     * Check if the caller is the course admin of the course that owns the student's course section. Only she may
     * delete the student (BR-student-lifecycle); being assigned to the course section is not enough.
     *
     * @param studentId the id of the student we need to access, this is NOT the current user's Id
     * @return true only when every link resolves and the course admin is the caller
     */
    public boolean isCurrentUserCourseAdminOfStudent(Integer studentId) {
        Integer userIdFromJwt = this.userUtils.getUserId();
        Student student = this.studentRepository.findById(studentId).orElse(null);
        if (student == null || student.getSection() == null || student.getSection().getCourse() == null
                || student.getSection().getCourse().getCourseAdmin() == null) {
            return false;
        }
        return student.getSection().getCourse().getCourseAdmin().getId().equals(userIdFromJwt);
    }

}
