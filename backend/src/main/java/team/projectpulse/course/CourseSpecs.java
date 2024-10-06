package team.projectpulse.course;

import team.projectpulse.instructor.Instructor;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecs {

    public static Specification<Course> containsCourseName(String providedCourseName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("courseName")), "%" + providedCourseName.toLowerCase() + "%");
    }

    public static Specification<Course> containsCourseDescription(String providedCourseDescription) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("courseDescription")), "%" + providedCourseDescription.toLowerCase() + "%");
    }

    public static Specification<Course> hasInstructor(Instructor instructor) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isMember(instructor, root.get("instructors"));
    }

}
