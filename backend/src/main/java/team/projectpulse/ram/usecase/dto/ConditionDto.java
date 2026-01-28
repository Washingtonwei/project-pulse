package team.projectpulse.ram.usecase.dto;

import jakarta.validation.constraints.NotEmpty;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.ram.requirement.RequirementArtifactType;

public record ConditionDto(
        Long id,
        @NotEmpty(message = "Condition description is required.")
        String condition,
        RequirementArtifactType type,
        Priority priority,
        String notes) {
}
