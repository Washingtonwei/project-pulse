package team.projectpulse.course;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Course courseToBeAccessed = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
        Integer adminId = courseToBeAccessed.getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    public boolean isCourseInstructor(Integer courseId) {
        Course courseToBeAccessed = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
        // Get the list of instructorIds from the course object
        List<Integer> instructorIds = courseToBeAccessed.getInstructors().stream()
                .map(Instructor::getId)
                .collect(Collectors.toList());
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        return instructorIds.contains(instructorIdFromJwt);
    }

}
