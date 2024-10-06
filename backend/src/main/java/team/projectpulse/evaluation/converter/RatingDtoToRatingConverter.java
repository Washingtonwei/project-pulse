package team.projectpulse.evaluation.converter;

import team.projectpulse.evaluation.dto.RatingDto;
import team.projectpulse.rubric.CriterionRepository;
import team.projectpulse.rubric.Rating;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RatingDtoToRatingConverter implements Converter<RatingDto, Rating> {

    private final CriterionRepository criterionRepository;


    public RatingDtoToRatingConverter(CriterionRepository criterionRepository) {
        this.criterionRepository = criterionRepository;
    }

    @Override
    public Rating convert(RatingDto ratingDto) {
        Rating rating = new Rating();
        rating.setRatingId(ratingDto.ratingId());
        rating.setCriterion(this.criterionRepository.findById(ratingDto.criterionId())
                .orElseThrow(() -> new ObjectNotFoundException("criterion", ratingDto.criterionId())));
        rating.setActualScore(ratingDto.actualScore());
        return rating;
    }

}
