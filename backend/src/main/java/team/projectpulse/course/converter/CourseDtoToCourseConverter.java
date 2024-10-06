package team.projectpulse.course.converter;

import team.projectpulse.course.Course;
import team.projectpulse.course.dto.CourseDto;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.system.UserUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CourseDtoToCourseConverter implements Converter<CourseDto, Course> {

    private final UserUtils userUtils;


    public CourseDtoToCourseConverter(UserUtils userUtils) {
        this.userUtils = userUtils;
    }

    @Override
    public Course convert(CourseDto courseDto) {
        Instructor instructor = this.userUtils.getInstructor();

        Course course = new Course(courseDto.courseName(), courseDto.courseDescription());
        course.setCourseId(courseDto.courseId());

        if (courseDto.courseId() == null) {
            course.setCourseAdmin(instructor);
        }
        return course;
    }

}
