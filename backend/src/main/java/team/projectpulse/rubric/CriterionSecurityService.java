package team.projectpulse.rubric;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Criterion criterionToBeAccessed = this.criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));
        Integer adminId = criterionToBeAccessed.getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    // Check if the user can access the criterion
    public boolean canAccessCriterion(Integer criterionId) {
        Criterion criterionToBeAccessed = this.criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));
        // Get the list of instructorIds from the criterion object
        List<Integer> instructorIds = criterionToBeAccessed.getCourse().getInstructors().stream()
                .map(Instructor::getId)
                .collect(Collectors.toList());
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        return instructorIds.contains(instructorIdFromJwt);
    }

}
