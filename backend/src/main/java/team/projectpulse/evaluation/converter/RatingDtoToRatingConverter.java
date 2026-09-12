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
        // The id is not read from the payload: a create has the server assign it, and an update scores the
        // evaluation's own rating rows in place. Mapping it here made every rating row in the database reachable
        // by id from a request body, and PeerEvaluation cascades to its ratings.
        Rating rating = new Rating();
        rating.setCriterion(this.criterionRepository.findById(ratingDto.criterionId())
                .orElseThrow(() -> new ObjectNotFoundException("criterion", ratingDto.criterionId())));
        rating.setActualScore(ratingDto.actualScore());
        return rating;
    }

}
