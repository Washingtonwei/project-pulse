package team.projectpulse.activity;

import org.springframework.data.jpa.domain.Specification;

public class ActivitySpecs {

    public static Specification<Activity> hasWeek(String providedWeek) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("week"), providedWeek);
    }

    public static Specification<Activity> hasTeamId(String providedTeamId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("team").get("teamId"), providedTeamId);
    }

    public static Specification<Activity> hasStudentId(String providedStudentId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("student").get("id"), providedStudentId);
    }

    public static Specification<Activity> hasSectionId(Integer providedSectionId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("team").get("section").get("sectionId"), providedSectionId);
    }

    public static Specification<Activity> hasWeekBetween(String startWeek, String endWeek) {
        return (root, query, criteriaBuilder) -> {
            if (startWeek.equals(endWeek)) {
                return criteriaBuilder.equal(root.get("week"), startWeek);
            } else {
                return criteriaBuilder.between(root.get("week"), startWeek, endWeek);
            }
        };
    }
    
}
