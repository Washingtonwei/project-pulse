package team.projectpulse.course;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserUtils userUtils;


    public CourseService(CourseRepository courseRepository, UserUtils userUtils) {
        this.courseRepository = courseRepository;
        this.userUtils = userUtils;
    }

    public Page<Course> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Instructor instructor = this.userUtils.getInstructor();

        Specification<Course> spec = Specification.unrestricted(); // Start with an unrestricted specification, matching all objects.

        if (StringUtils.hasLength(searchCriteria.get("courseName"))) {
            spec = spec.and(CourseSpecs.containsCourseName(searchCriteria.get("courseName")));
        }

        if (StringUtils.hasLength(searchCriteria.get("courseDescription"))) {
            spec = spec.and(CourseSpecs.containsCourseDescription(searchCriteria.get("courseDescription")));
        }

        spec = spec.and(CourseSpecs.hasInstructor(instructor)); // Only show courses that the instructor belongs to

        return this.courseRepository.findAll(spec, pageable);
    }

    public Course saveCourse(Course course) {
        return this.courseRepository.save(course);
    }

    public Course findCourseById(Integer courseId) {
        return this.courseRepository.findById(courseId).orElse(null);
    }

    public Course updateCourse(Integer courseId, Course update) {
        return this.courseRepository.findById(courseId)
                .map(oldCourse -> {
                    oldCourse.setCourseName(update.getCourseName());
                    oldCourse.setCourseDescription(update.getCourseDescription());
                    return this.courseRepository.save(oldCourse);
                }).orElseThrow(() -> new ObjectNotFoundException("course", courseId));
    }

    public void deleteCourse(Integer courseId) {
        this.courseRepository.findById(courseId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", courseId));
        this.courseRepository.deleteById(courseId);
    }

}
