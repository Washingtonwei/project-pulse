package team.projectpulse.rubric.converter;

import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.dto.CriterionDto;
import team.projectpulse.rubric.dto.RubricDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RubricToRubricDtoConverter implements Converter<Rubric, RubricDto> {

    private final CriterionToCriterionDtoConverter criterionToCriterionDtoConverter;


    public RubricToRubricDtoConverter(CriterionToCriterionDtoConverter criterionToCriterionDtoConverter) {
        this.criterionToCriterionDtoConverter = criterionToCriterionDtoConverter;
    }

    @Override
    public RubricDto convert(Rubric rubric) {
        Set<CriterionDto> criteria = rubric.getCriteria().stream()
                .map(criterionToCriterionDtoConverter::convert).collect(Collectors.toSet());
        return new RubricDto(
                rubric.getRubricId(),
                rubric.getRubricName(),
                criteria,
                rubric.getCourse().getCourseId()
        );
    }

}
