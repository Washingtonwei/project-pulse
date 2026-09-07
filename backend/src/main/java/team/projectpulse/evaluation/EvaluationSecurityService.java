package team.projectpulse.evaluation;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the peer evaluation question the route rules ask: did the caller write this evaluation
 * ({@code isEvaluationOwner})? The course-section variant, {@code canAccessEvaluationsInSection}, has no
 * caller today: the course section's weekly report route uses {@code SectionSecurityService.isSectionInstructor}
 * instead.
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
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
        PeerEvaluation evaluation = this.evaluationRepository.findById(evaluationId).orElse(null);
        if (evaluation == null) {
            return false;
        }
        Integer ownerId = evaluation.getEvaluator().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return ownerId.equals(userIdFromJwt);
    }

    // Only instructors of the section can access evaluations
    public boolean canAccessEvaluationsInSection(Integer sectionId) {
        Integer userIdFromJwt = this.userUtils.getUserId();
        Section section = this.sectionRepository.findById(sectionId).orElse(null);
        if (section == null) {
            return false;
        }
        List<Integer> instructorIds = section.getInstructors().stream()
                .map(Instructor::getId).collect(Collectors.toList());
        return instructorIds.contains(userIdFromJwt);
    }

}
