package team.projectpulse.instructor.dto;

import jakarta.validation.constraints.NotEmpty;

public record InstructorDto(Integer id,
                            @NotEmpty(message = "Username is required.")
                            String username,
                            @NotEmpty(message = "First rubricName is required.")
                            String firstName,
                            @NotEmpty(message = "Last rubricName is required.")
                            String lastName,
                            @NotEmpty(message = "Email is required.")
                            String email,
                            boolean enabled,
                            String roles,
                            Integer defaultSectionId,
                            Integer defaultCourseId) {
}
