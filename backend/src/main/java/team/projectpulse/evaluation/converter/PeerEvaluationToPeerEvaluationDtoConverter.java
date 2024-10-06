package team.projectpulse.evaluation.converter;

import team.projectpulse.evaluation.PeerEvaluation;
import team.projectpulse.evaluation.dto.PeerEvaluationDto;
import team.projectpulse.evaluation.dto.RatingDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PeerEvaluationToPeerEvaluationDtoConverter implements Converter<PeerEvaluation, PeerEvaluationDto> {

    private final RatingToRatingDtoConverter ratingToRatingDtoConverter;


    public PeerEvaluationToPeerEvaluationDtoConverter(RatingToRatingDtoConverter ratingToRatingDtoConverter) {
        this.ratingToRatingDtoConverter = ratingToRatingDtoConverter;
    }

    @Override
    public PeerEvaluationDto convert(PeerEvaluation peerEvaluation) {
        List<RatingDto> ratingDtos = peerEvaluation.getRatings().stream()
                .map(this.ratingToRatingDtoConverter::convert)
                .collect(Collectors.toList());
        return new PeerEvaluationDto(peerEvaluation.getPeerEvaluationId(),
                peerEvaluation.getWeek(),
                peerEvaluation.getEvaluator().getId(),
                peerEvaluation.getEvaluator().getFirstName() + " " + peerEvaluation.getEvaluator().getLastName(),
                peerEvaluation.getEvaluatee().getId(),
                peerEvaluation.getEvaluatee().getFirstName() + " " + peerEvaluation.getEvaluatee().getLastName(),
                ratingDtos,
                peerEvaluation.getTotalScore(),
                peerEvaluation.getPublicComment(),
                peerEvaluation.getPrivateComment(),
                peerEvaluation.getCreatedAt(),
                peerEvaluation.getUpdatedAt()
        );
    }

}
