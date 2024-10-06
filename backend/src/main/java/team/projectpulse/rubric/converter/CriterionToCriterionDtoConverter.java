package team.projectpulse.rubric.converter;

import team.projectpulse.rubric.Criterion;
import team.projectpulse.rubric.dto.CriterionDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CriterionToCriterionDtoConverter implements Converter<Criterion, CriterionDto> {

    @Override
    public CriterionDto convert(Criterion criterion) {
        return new CriterionDto(
                criterion.getCriterionId(),
                criterion.getCriterion(),
                criterion.getDescription(),
                criterion.getMaxScore(),
                criterion.getCourse().getCourseId()
        );
    }

}
