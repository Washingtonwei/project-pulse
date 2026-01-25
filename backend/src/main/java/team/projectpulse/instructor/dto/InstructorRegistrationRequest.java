package team.projectpulse.instructor.dto;

import jakarta.validation.constraints.NotEmpty;

public record InstructorRegistrationRequest(@NotEmpty(message = "Username must not be empty")
                                            String username,
                                            @NotEmpty(message = "First name must not be empty")
                                            String firstName,
                                            @NotEmpty(message = "Last name must not be empty")
                                            String lastName,
                                            @NotEmpty(message = "Email must not be empty")
                                            String email,
                                            @NotEmpty(message = "Password must not be empty")
                                            String password) {
}
