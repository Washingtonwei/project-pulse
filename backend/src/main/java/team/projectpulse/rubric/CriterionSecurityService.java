package team.projectpulse.rubric;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the rubric criterion questions the route rules ask: is the caller the admin of the course that owns this
 * criterion ({@code isCriterionOwner}), and does the caller teach that course ({@code canAccessCriterion}).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class CriterionSecurityService {

    private final CriterionRepository criterionRepository;
    private final UserUtils userUtils;


    public CriterionSecurityService(CriterionRepository criterionRepository, UserUtils userUtils) {
        this.criterionRepository = criterionRepository;
        this.userUtils = userUtils;
    }

    public boolean isCriterionOwner(Integer criterionId) {
        Criterion criterionToBeAccessed = this.criterionRepository.findById(criterionId).orElse(null);
        if (criterionToBeAccessed == null) {
            return false;
        }
        Integer adminId = criterionToBeAccessed.getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    // Check if the user can access the criterion
    public boolean canAccessCriterion(Integer criterionId) {
        Criterion criterionToBeAccessed = this.criterionRepository.findById(criterionId).orElse(null);
        if (criterionToBeAccessed == null) {
            return false;
        }
        // Get the list of instructorIds from the criterion object
        List<Integer> instructorIds = criterionToBeAccessed.getCourse().getInstructors().stream()
                .map(Instructor::getId)
                .collect(Collectors.toList());
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        return instructorIds.contains(instructorIdFromJwt);
    }

}
