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
public class RubricService {

    private final RubricRepository rubricRepository;
    private final CriterionRepository criterionRepository;
    private final UserUtils userUtils;


    public RubricService(RubricRepository rubricRepository, CriterionRepository criterionRepository, UserUtils userUtils) {
        this.rubricRepository = rubricRepository;
        this.criterionRepository = criterionRepository;
        this.userUtils = userUtils;
    }

    public Page<Rubric> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Integer courseId = this.userUtils.getUserCourseId();

        Specification<Rubric> spec = Specification.unrestricted(); // Start with an unrestricted specification, matching all objects.

        if (StringUtils.hasLength(searchCriteria.get("rubricName"))) {
            spec = spec.and(RubricSpecs.containsRubricName(searchCriteria.get("rubricName")));
        }

        spec = spec.and(RubricSpecs.belongsToCourse(courseId)); // Only show rubrics that belong to the default course of the instructor

        return this.rubricRepository.findAll(spec, pageable);
    }

    public Rubric findRubricById(Integer rubricId) {
        return this.rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
    }

    public Rubric saveRubric(Rubric newRubric) {
        return this.rubricRepository.save(newRubric);
    }

    public Rubric updateRubric(Integer rubricId, Rubric update) {
        return this.rubricRepository.findById(rubricId)
                .map(oldRubric -> {
                    oldRubric.setRubricName(update.getRubricName());
                    return this.rubricRepository.save(oldRubric);
                })
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
    }

    public void deleteRubric(Integer rubricId) {
        this.rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
        this.rubricRepository.deleteById(rubricId);
    }

    public void addCriterionToRubric(Integer rubricId, Integer criterionId) {
        Rubric rubric = this.rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
        Criterion criterion = this.criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));

        // Check if the criterion is already in the rubric
        if (!rubric.getCriteria().contains(criterion)) {
            rubric.addCriterion(criterion);
        }
    }

    public void removeCriterionFromRubric(Integer rubricId, Integer criterionId) {
        Rubric rubric = this.rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
        Criterion criterion = this.criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ObjectNotFoundException("criterion", criterionId));

        // Check if the criterion is in the rubric
        if (rubric.getCriteria().contains(criterion)) {
            rubric.removeCriterion(criterion);
        }
    }

}
