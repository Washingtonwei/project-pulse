package team.projectpulse.ram.requirement.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.requirement.RequirementArtifactType;

/**
 * A press of Suggest: what the student has written so far about the artifact whose key she wants suggested
 * (UC-ART-create-artifact, "Artifact key rules"). Either the title or the content may be blank, but not both. Any type
 * is accepted, since every type's editor offers Suggest. For example:
 * <pre>
 * POST /teams/1/requirement-artifacts/key-suggestion
 * {"type": "FUNCTIONAL_REQUIREMENT", "title": "Data export", "content": "The system shall let the client export project data as a CSV file."}
 * </pre>
 *
 * @param type    the artifact's type, which sets the key's prefix
 * @param title   its title (a glossary term's term, a use case's name); may be blank
 * @param content its content (a definition, a description, a condition's wording); may be blank
 */
public record ArtifactKeySuggestionRequest(
        @NotNull(message = "Requirement artifact type must not be null")
        RequirementArtifactType type,
        String title,
        String content) {
}
