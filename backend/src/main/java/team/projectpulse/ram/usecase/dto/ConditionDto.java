package team.projectpulse.ram.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.ram.requirement.RequirementArtifactType;

import java.util.List;

/**
 * A precondition or postcondition of a use case. In a request only {@code id} (optional: an existing condition of the
 * team) and {@code condition} (the wording) are read; the other fields are response only.
 *
 * @param otherUseCases the other use cases that reference this condition; empty when it is not shared
 */
public record ConditionDto(
        Long id,
        String artifactKey,
        @NotBlank(message = "Condition wording is required.")
        String condition,
        RequirementArtifactType type,
        Priority priority,
        String notes,
        List<OtherUseCaseDto> otherUseCases) {

    public ConditionDto {
        otherUseCases = otherUseCases == null ? List.of() : List.copyOf(otherUseCases);
    }
}
