package team.projectpulse.course;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the course questions the route rules ask: is the caller this course's admin ({@code isCourseOwner}), and
 * does the caller teach it ({@code isCourseInstructor}).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class CourseSecurityService {

    private final CourseRepository courseRepository;
    private final UserUtils userUtils;


    public CourseSecurityService(CourseRepository courseRepository, UserUtils userUtils) {
        this.courseRepository = courseRepository;
        this.userUtils = userUtils;
    }

    public boolean isCourseOwner(Integer courseId) {
        Course courseToBeAccessed = this.courseRepository.findById(courseId).orElse(null);
        if (courseToBeAccessed == null) {
            return false;
        }
        Integer adminId = courseToBeAccessed.getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    public boolean isCourseInstructor(Integer courseId) {
        Course courseToBeAccessed = this.courseRepository.findById(courseId).orElse(null);
        if (courseToBeAccessed == null) {
            return false;
        }
        // Get the list of instructorIds from the course object
        List<Integer> instructorIds = courseToBeAccessed.getInstructors().stream()
                .map(Instructor::getId)
                .collect(Collectors.toList());
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        return instructorIds.contains(instructorIdFromJwt);
    }

}
