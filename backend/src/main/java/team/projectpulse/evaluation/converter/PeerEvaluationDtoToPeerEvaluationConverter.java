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
        // Not a student, so not an evaluator: an instructor or course admin does not author peer evaluations
        // (BR-team-assignment-required). Say that, rather than reporting the caller's own id as a missing student.
        Student evaluator = this.studentRepository.findById(evaluatorId)
                .orElseThrow(() -> new PeerEvaluationIllegalArgumentException("Only a student may submit a peer evaluation."));
        Student evaluatee = this.studentRepository.findById(peerEvaluationDto.evaluateeId()).orElseThrow(() -> new ObjectNotFoundException("student", peerEvaluationDto.evaluateeId()));

        // Convert the list of ratingDtos to a list of ratings
        List<Rating> ratings = peerEvaluationDto.ratings().stream()
                .map(this.ratingDtoToRatingConverter::convert)
                .collect(Collectors.toList());

        // Check that all criteria in the rubric are rated and that each criterion is rated only once
        Section section = evaluator.getSection(); // Get the section of the evaluator
        Set<Integer> sectionRubricCriterionIds = section.getRubric().getCriteria().stream().map(Criterion::getCriterionId).collect(Collectors.toSet());
        Set<Integer> ratedCriterionIds = ratings.stream().map(Rating::getCriterion).map(Criterion::getCriterionId).collect(Collectors.toSet());
        // Two questions, and neither comparison answers the other. The set equality asks *which* criteria were
        // rated, catching one that is missing or one that belongs to another rubric. The size comparison asks
        // *how many times* each was rated: `ratings` is the submitted list and `ratedCriterionIds` is that same
        // list deduplicated, so any difference between the two counts means some criterion was rated more than
        // once. Set equality cannot see a repeat, because the duplicate is already gone by the time the set is
        // built (seven ratings covering six criteria compare equal to a six-criterion rubric), and the size
        // comparison cannot see a missing criterion, because five ratings for five criteria have matching
        // counts. Both halves are needed to make this message true.
        if (ratings.size() != ratedCriterionIds.size() || !sectionRubricCriterionIds.equals(ratedCriterionIds)) {
            throw new PeerEvaluationIllegalArgumentException("The ratings are not valid. Please make sure all criteria in the rubric are rated and each criterion is rated only once.");
        }

        // If everything is valid, create a peer evaluation object for adding or updating.
        // The id is not read from the payload: a create has the server assign it, and an update takes it from the
        // URL. Mapping it here would let a create carry an existing id, which turns save() into a merge over that
        // evaluation. The evaluator and evaluatee are therefore always stamped from the caller rather than behind
        // a payload-id guard; EvaluationService.updatePeerEvaluation reads neither, since it updates the
        // evaluation the URL names.
        PeerEvaluation peerEvaluation = new PeerEvaluation();
        peerEvaluation.setWeek(peerEvaluationDto.week());
        peerEvaluation.setRatings(ratings);
        peerEvaluation.setPublicComment(peerEvaluationDto.publicComment());
        peerEvaluation.setPrivateComment(peerEvaluationDto.privateComment());
        peerEvaluation.setEvaluator(evaluator);
        peerEvaluation.setEvaluatee(evaluatee);
        return peerEvaluation;
    }

}
