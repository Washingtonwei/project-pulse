package team.projectpulse.course.converter;

import team.projectpulse.course.Course;
import team.projectpulse.course.dto.CourseDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CourseToCourseDtoConverter implements Converter<Course, CourseDto> {

    @Override
    public CourseDto convert(Course course) {
        return new CourseDto(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseDescription()
        );
    }

}
