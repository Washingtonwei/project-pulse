package team.projectpulse.course.dto;

import jakarta.validation.constraints.NotEmpty;

public record CourseDto(
        Integer courseId,
        @NotEmpty(message = "course name is required.")
        String courseName,
        @NotEmpty(message = "course description is required.")
        String courseDescription) {
}
