package team.projectpulse.rubric;

import org.springframework.data.jpa.domain.Specification;

public class CriterionSpecs {

    public static Specification<Criterion> containsCriterion(String providedCriterion) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("criterion")), "%" + providedCriterion.toLowerCase() + "%");
    }

    public static Specification<Criterion> containsDescription(String providedDescription) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + providedDescription.toLowerCase() + "%");
    }

    public static Specification<Criterion> belongsToCourse(Integer courseId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("course").get("id"), courseId);
    }

}
