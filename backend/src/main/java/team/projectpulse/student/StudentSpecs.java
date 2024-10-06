package team.projectpulse.student;

import org.springframework.data.jpa.domain.Specification;

public class StudentSpecs {

    public static Specification<Student> containsFirstName(String providedFirstName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + providedFirstName.toLowerCase() + "%");
    }

    public static Specification<Student> containsLastName(String providedLastName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + providedLastName.toLowerCase() + "%");
    }

    public static Specification<Student> hasEmail(String providedEmail) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("email")), providedEmail.toLowerCase());
    }

    public static Specification<Student> hasTeamName(String providedTeamName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("team").get("teamName")), providedTeamName.toLowerCase());
    }

    public static Specification<Student> hasTeamId(String providedTeamId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("team").get("teamId"), providedTeamId);
    }

    public static Specification<Student> hasSectionName(String providedSectionName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("section").get("sectionName")), providedSectionName.toLowerCase());
    }

    public static Specification<Student> hasSectionId(String providedSectionId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("section").get("sectionId"), providedSectionId);
    }

    public static Specification<Student> belongsToSection(Integer providedSectionId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("section").get("sectionId"), providedSectionId);
    }

}
