package team.projectpulse.evaluation;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvaluationSecurityService {

    private final PeerEvaluationRepository evaluationRepository;
    private final UserUtils userUtils;
    private final SectionRepository sectionRepository;


    public EvaluationSecurityService(PeerEvaluationRepository evaluationRepository, UserUtils userUtils, SectionRepository sectionRepository) {
        this.evaluationRepository = evaluationRepository;
        this.userUtils = userUtils;
        this.sectionRepository = sectionRepository;
    }

    public boolean isEvaluationOwner(Integer evaluationId) {
        PeerEvaluation evaluation = this.evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ObjectNotFoundException("evaluation", evaluationId));
        Integer ownerId = evaluation.getEvaluator().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return ownerId.equals(userIdFromJwt);
    }

    // Only instructors of the section can access evaluations
    public boolean canAccessEvaluationsInSection(Integer sectionId) {
        Integer userIdFromJwt = this.userUtils.getUserId();
        Section section = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        List<Integer> instructorIds = section.getInstructors().stream()
                .map(Instructor::getId).collect(Collectors.toList());
        return instructorIds.contains(userIdFromJwt);
    }

}
