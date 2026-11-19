package team.projectpulse.ram.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;

/**
 * A requirement artifact on the wire. In a request only {@code type}, {@code title}, {@code content},
 * {@code priority}, {@code notes}, and, on an update, {@code version} are read; an {@code id}, {@code artifactKey} or
 * {@code homeDocumentType} in a body is ignored (OI-46).
 *
 * @param homeDocumentType response only: the type of the document that holds the artifact's home section
 *                         (BR-artifact-home), null for an artifact without one. A request never needs it, since the
 *                         type alone names the home.
 * @param version          the artifact's optimistic-lock version (DI-concurrency-control); an update must send it.
 */
public record RequirementArtifactDto(
        Long id,
        @NotNull(message = "Requirement artifact type must not be null")
        RequirementArtifactType type,
        String artifactKey,
        @NotBlank(message = "Requirement artifact title must not be empty")
        @Size(max = 255, message = "Requirement artifact title must be at most 255 characters")
        String title,
        @NotBlank(message = "Requirement artifact content must not be empty")
        String content,
        Priority priority,
        DocumentType homeDocumentType,
        String notes,
        Integer version,
        Instant createdAt,
        Instant updatedAt,
        PeerEvaluationUserDto createdBy,
        PeerEvaluationUserDto updatedBy) {
}
