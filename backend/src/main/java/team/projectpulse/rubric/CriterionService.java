package team.projectpulse.rubric;

import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@Transactional
public class CriterionService {

    private final CriterionRepository criterionRepository;
    private final UserUtils userUtils;


    public CriterionService(CriterionRepository criterionRepository, UserUtils userUtils) {
        this.criterionRepository = criterionRepository;
        this.userUtils = userUtils;
    }

    public Page<Criterion> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Integer courseId = this.userUtils.getUserCourseId();

        Specification<Criterion> spec = Specification.unrestricted(); // Start with an unrestricted specification, matching all objects.

        if (StringUtils.hasLength(searchCriteria.get("criterion"))) {
            spec = spec.and(CriterionSpecs.containsCriterion(searchCriteria.get("criterion")));
        }

        if (StringUtils.hasLength(searchCriteria.get("description"))) {
            spec = spec.and(CriterionSpecs.containsDescription(searchCriteria.get("description")));
        }

        spec = spec.and(CriterionSpecs.belongsToCourse(courseId)); // Only show criteria that belong to the default course of the instructor

        return this.criterionRepository.findAll(spec, pageable);
    }

    public Criterion findCriterionById(Integer criterionId) {
        return this.criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));
    }

    public Criterion saveCriterion(Criterion newCriterion) {
        return this.criterionRepository.save(newCriterion);
    }

    public Criterion updateCriterion(Integer criterionId, Criterion update) {
        return this.criterionRepository.findById(criterionId)
                .map(oldCriterion -> {
                    oldCriterion.setCriterion(update.getCriterion());
                    oldCriterion.setDescription(update.getDescription());
                    oldCriterion.setMaxScore(update.getMaxScore());
                    return this.criterionRepository.save(oldCriterion);
                })
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));
    }

    public void deleteCriterion(Integer criterionId) {
        this.criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));
        this.criterionRepository.deleteById(criterionId);
    }

}
