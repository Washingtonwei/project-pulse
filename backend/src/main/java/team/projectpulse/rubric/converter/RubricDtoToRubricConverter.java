package team.projectpulse.rubric.converter;

import team.projectpulse.course.Course;
import team.projectpulse.course.CourseRepository;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.dto.RubricDto;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RubricDtoToRubricConverter implements Converter<RubricDto, Rubric> {

    private final UserUtils userUtils;
    private final CourseRepository courseRepository;


    public RubricDtoToRubricConverter(UserUtils userUtils, CourseRepository courseRepository) {
        this.userUtils = userUtils;
        this.courseRepository = courseRepository;
    }

    @Override
    public Rubric convert(RubricDto rubricDto) {
        Rubric rubric = new Rubric(rubricDto.rubricName());
        rubric.setRubricId(rubricDto.rubricId());
        if (rubricDto.rubricId() == null) { // When creating a new rubric, find and set the course; when updating a rubric, don't set the course
            Integer courseId = userUtils.getUserCourseId();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
            course.addRubric(rubric);
        }
        return rubric;
    }

}
