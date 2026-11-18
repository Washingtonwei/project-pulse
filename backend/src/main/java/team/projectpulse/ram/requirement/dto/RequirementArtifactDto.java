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
 * {@code priority}, {@code notes}, and, depending on the endpoint, {@code homeDocumentType} (create) or
 * {@code version} (update) are read; an {@code id} or {@code artifactKey} in a body is ignored (OI-46).
 *
 * @param homeDocumentType the type of the document that holds the artifact's home section (BR-artifact-home). On a
 *                         create it names the home for a two-home type; in a response it is filled for every artifact
 *                         with a home and null for one without.
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
