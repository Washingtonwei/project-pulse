package team.projectpulse.ram.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.ram.requirement.RequirementArtifactType;

import java.util.List;

/**
 * A precondition or postcondition of a use case. In a request only {@code id} (optional: an existing condition of the
 * team), {@code condition} (the wording) and {@code artifactKey} are read; the wording and key matter only for a new
 * condition (no {@code id}), which must carry a key such as {@code PRE-student-logged-in}, and an existing condition
 * keeps its stored ones. The other fields are response only.
 *
 * @param artifactKey a new condition's artifact key (required for it); for an existing one, the stored key in a
 *                    response and ignored in a request
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
