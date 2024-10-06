package team.projectpulse.rubric.converter;

import team.projectpulse.course.Course;
import team.projectpulse.course.CourseRepository;
import team.projectpulse.rubric.Criterion;
import team.projectpulse.rubric.dto.CriterionDto;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CriterionDtoToCriterionConverter implements Converter<CriterionDto, Criterion> {

    private final UserUtils userUtils;
    private final CourseRepository courseRepository;


    public CriterionDtoToCriterionConverter(UserUtils userUtils, CourseRepository courseRepository) {
        this.userUtils = userUtils;
        this.courseRepository = courseRepository;
    }

    @Override
    public Criterion convert(CriterionDto criterionDto) {
        Criterion criterion = new Criterion(
                criterionDto.criterion(),
                criterionDto.description(),
                criterionDto.maxScore()
        );
        criterion.setCriterionId(criterionDto.criterionId());
        if (criterionDto.criterionId() == null) { // When creating a new criterion, find and set the course; when updating a criterion, don't set the course
            Integer courseId = userUtils.getUserCourseId();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
            course.addCriterion(criterion);
        }
        return criterion;
    }

}
