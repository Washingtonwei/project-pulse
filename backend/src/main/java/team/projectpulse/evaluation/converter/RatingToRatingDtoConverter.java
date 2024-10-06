package team.projectpulse.evaluation.converter;

import team.projectpulse.evaluation.dto.RatingDto;
import team.projectpulse.rubric.Rating;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RatingToRatingDtoConverter implements Converter<Rating, RatingDto> {

    @Override
    public RatingDto convert(Rating rating) {
        return new RatingDto(rating.getRatingId(),
                rating.getCriterion().getCriterionId(),
                rating.getActualScore()
        );
    }
    
}
