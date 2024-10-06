package team.projectpulse.instructor;

import team.projectpulse.course.Course;
import org.springframework.data.jpa.domain.Specification;

public class InstructorSpecs {

    public static Specification<Instructor> hasFirstName(String providedFirstName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("firstName")), providedFirstName.toLowerCase());
    }

    public static Specification<Instructor> hasLastName(String providedLastName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("lastName")), providedLastName.toLowerCase());
    }

    public static Specification<Instructor> hasEmail(String providedEmail) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("email")), providedEmail.toLowerCase());
    }

    public static Specification<Instructor> hasCourse(Course course) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isMember(course, root.get("courses"));
    }

}
