package team.projectpulse.ram.requirement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;

public record RequirementArtifactDto(
        Long id,
        @NotNull(message = "Requirement artifact type must not be null")
        RequirementArtifactType type,
        String artifactKey,
        @NotEmpty(message = "Requirement artifact title must not be empty")
        String title,
        @NotEmpty(message = "Requirement artifact content must not be empty")
        String content,
        Priority priority,
        Long sourceSectionId,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        PeerEvaluationUserDto createdBy,
        PeerEvaluationUserDto updatedBy) {
}

