package team.projectpulse.evaluation.converter;

import team.projectpulse.evaluation.PeerEvaluation;
import team.projectpulse.evaluation.dto.PeerEvaluationDto;
import team.projectpulse.rubric.Criterion;
import team.projectpulse.rubric.Rating;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.PeerEvaluationIllegalArgumentException;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PeerEvaluationDtoToPeerEvaluationConverter implements Converter<PeerEvaluationDto, PeerEvaluation> {

    private final RatingDtoToRatingConverter ratingDtoToRatingConverter;
    private final StudentRepository studentRepository;
    private final UserUtils userUtils;


    public PeerEvaluationDtoToPeerEvaluationConverter(RatingDtoToRatingConverter ratingDtoToRatingConverter, StudentRepository studentRepository, UserUtils userUtils) {
        this.ratingDtoToRatingConverter = ratingDtoToRatingConverter;
        this.studentRepository = studentRepository;
        this.userUtils = userUtils;
    }

    @Override
    public PeerEvaluation convert(PeerEvaluationDto peerEvaluationDto) {
        // Get the evaluator or evaluation submitter ID from the JWT token
        Integer evaluatorId = this.userUtils.getUserId();

        // Find the evaluator and evaluatee
        Student evaluator = this.studentRepository.findById(evaluatorId).orElseThrow(() -> new ObjectNotFoundException("student", evaluatorId));
        Student evaluatee = this.studentRepository.findById(peerEvaluationDto.evaluateeId()).orElseThrow(() -> new ObjectNotFoundException("student", peerEvaluationDto.evaluateeId()));

        // Convert the list of ratingDtos to a list of ratings
        List<Rating> ratings = peerEvaluationDto.ratings().stream()
                .map(this.ratingDtoToRatingConverter::convert)
                .collect(Collectors.toList());

        // Check if all criteria in the rubric are rated and each criterion is rated only once
        Section section = evaluator.getSection(); // Get the section of the evaluator
        Set<Integer> sectionRubricCriterionIdList = section.getRubric().getCriteria().stream().map(Criterion::getCriterionId).collect(Collectors.toSet());
        Set<Integer> ratedCriterionIdList = ratings.stream().map(Rating::getCriterion).map(Criterion::getCriterionId).collect(Collectors.toSet());
        if (!sectionRubricCriterionIdList.equals(ratedCriterionIdList)) {
            throw new PeerEvaluationIllegalArgumentException("The ratings are not valid. Please make sure all criteria in the rubric are rated and each criterion is rated only once.");
        }

        // If everything is valid, create a peer evaluation object for adding or updating
        PeerEvaluation peerEvaluation = new PeerEvaluation();
        peerEvaluation.setPeerEvaluationId(peerEvaluationDto.evaluationId());
        peerEvaluation.setWeek(peerEvaluationDto.week());
        peerEvaluation.setRatings(ratings);
        peerEvaluation.setPublicComment(peerEvaluationDto.publicComment());
        peerEvaluation.setPrivateComment(peerEvaluationDto.privateComment());
        if (peerEvaluationDto.evaluationId() == null) { // If it is a new evaluation, set the evaluator and evaluatee; for updating, the evaluator and evaluatee are not changed
            peerEvaluation.setEvaluator(evaluator);
            peerEvaluation.setEvaluatee(evaluatee);
        }
        return peerEvaluation;
    }

}
